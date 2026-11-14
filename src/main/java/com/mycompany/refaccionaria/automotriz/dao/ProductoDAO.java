package com.mycompany.refaccionaria.automotriz.dao;

import com.mycompany.refaccionaria.automotriz.config.DatabaseConnection;
import com.mycompany.refaccionaria.automotriz.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAO {

    private static final String BASE_SELECT = """
            SELECT id, codigo, nombre, marca, categoria, precio, stock, stock_minimo
            FROM productos
            """;

    public List<Producto> findAll() throws SQLException {
        return find(BASE_SELECT + "ORDER BY nombre");
    }

    public List<Producto> search(String searchTerm) throws SQLException {
        String sql = BASE_SELECT + """
                WHERE codigo LIKE ?
                   OR nombre LIKE ?
                   OR marca LIKE ?
                   OR categoria LIKE ?
                ORDER BY nombre
                """;
        String pattern = "%" + searchTerm.trim() + "%";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            return readList(statement);
        }
    }

    public Optional<Producto> findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    public Optional<Producto> findByCodigo(String codigo) throws SQLException {
        String sql = BASE_SELECT + "WHERE codigo = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codigo);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    public Producto save(Producto producto) throws SQLException {
        String sql = """
                INSERT INTO productos
                    (codigo, nombre, marca, categoria, precio, stock, stock_minimo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setProductParameters(statement, producto);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se pudo obtener el ID del producto creado.");
                }
                producto.setId(keys.getInt(1));
            }
        }
        return producto;
    }

    public void update(Producto producto) throws SQLException {
        String sql = """
                UPDATE productos
                SET codigo = ?, nombre = ?, marca = ?, categoria = ?,
                    precio = ?, stock = ?, stock_minimo = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            setProductParameters(statement, producto);
            statement.setInt(8, producto.getId());
            statement.executeUpdate();
        }
    }

    public void updateStock(Connection connection, int productoId, int stock) throws SQLException {
        String sql = "UPDATE productos SET stock = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, stock);
            statement.setInt(2, productoId);
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public boolean hasSales(int id) throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM detalle_ventas WHERE producto_id = ?)";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    public long count() throws SQLException {
        return scalarCount("SELECT COUNT(*) FROM productos");
    }

    public long countLowStock() throws SQLException {
        return scalarCount("SELECT COUNT(*) FROM productos WHERE stock <= stock_minimo");
    }

    public long countAvailable() throws SQLException {
        return scalarCount("SELECT COUNT(*) FROM productos WHERE stock > 0");
    }

    public long countOutOfStock() throws SQLException {
        return scalarCount("SELECT COUNT(*) FROM productos WHERE stock = 0");
    }

    public List<Producto> findLowStock() throws SQLException {
        return find(BASE_SELECT + """
                WHERE stock <= stock_minimo
                ORDER BY stock ASC, nombre ASC
                """);
    }

    private long scalarCount(String sql) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private List<Producto> find(String sql) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            return readList(statement);
        }
    }

    private List<Producto> readList(PreparedStatement statement) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                productos.add(map(resultSet));
            }
        }
        return productos;
    }

    private Producto map(ResultSet resultSet) throws SQLException {
        return new Producto(
                resultSet.getInt("id"),
                resultSet.getString("codigo"),
                resultSet.getString("nombre"),
                resultSet.getString("marca"),
                resultSet.getString("categoria"),
                resultSet.getBigDecimal("precio"),
                resultSet.getInt("stock"),
                resultSet.getInt("stock_minimo")
        );
    }

    private void setProductParameters(PreparedStatement statement, Producto producto) throws SQLException {
        statement.setString(1, producto.getCodigo());
        statement.setString(2, producto.getNombre());
        statement.setString(3, producto.getMarca());
        statement.setString(4, producto.getCategoria());
        statement.setBigDecimal(5, producto.getPrecio());
        statement.setInt(6, producto.getStock());
        statement.setInt(7, producto.getStockMinimo());
    }
}
