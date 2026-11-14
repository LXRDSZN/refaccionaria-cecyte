package com.mycompany.refaccionaria.automotriz.dao;

import com.mycompany.refaccionaria.automotriz.config.DatabaseConnection;
import com.mycompany.refaccionaria.automotriz.model.Venta;
import com.mycompany.refaccionaria.automotriz.model.DetalleVenta;
import com.mycompany.refaccionaria.automotriz.model.DetalleVentaResumen;
import com.mycompany.refaccionaria.automotriz.model.VentaResumen;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentaDAO {

    private static final String BASE_SELECT = """
            SELECT id, fecha, total, usuario_id
            FROM ventas
            """;

    public List<Venta> findAll() throws SQLException {
        List<Venta> ventas = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY fecha DESC, id DESC";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ventas.add(map(resultSet));
            }
        }
        return ventas;
    }

    public Optional<Venta> findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    public List<VentaResumen> findSummaries(Integer saleId, LocalDate from, LocalDate to) throws SQLException {
            StringBuilder sql = new StringBuilder("""
                    SELECT v.id, v.fecha, u.nombre_usuario, v.total
                    FROM ventas v
                    INNER JOIN usuarios u ON u.id = v.usuario_id
                    WHERE 1 = 1
                    """);
            List<Object> parameters = new ArrayList<>();
            if (saleId != null) {
                sql.append(" AND v.id = ?");
                parameters.add(saleId);
            }
            if (from != null) {
                sql.append(" AND v.fecha >= ?");
                parameters.add(Timestamp.valueOf(from.atStartOfDay()));
            }
            if (to != null) {
                sql.append(" AND v.fecha < ?");
                parameters.add(Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            }
            sql.append(" ORDER BY v.fecha DESC, v.id DESC");

            List<VentaResumen> summaries = new ArrayList<>();
            try (Connection connection = DatabaseConnection.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                setParameters(statement, parameters);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        summaries.add(new VentaResumen(
                                resultSet.getInt("id"),
                                resultSet.getTimestamp("fecha").toLocalDateTime(),
                                resultSet.getString("nombre_usuario"),
                                resultSet.getBigDecimal("total")
                        ));
                    }
                }
            }
            return summaries;
        }

    public List<DetalleVentaResumen> findDetails(int saleId) throws SQLException {
            String sql = """
                    SELECT v.id AS venta_id, v.fecha, u.nombre_usuario,
                           p.codigo, p.nombre, d.cantidad, d.precio_unitario,
                           d.subtotal, v.total
                    FROM ventas v
                    INNER JOIN usuarios u ON u.id = v.usuario_id
                    INNER JOIN detalle_ventas d ON d.venta_id = v.id
                    INNER JOIN productos p ON p.id = d.producto_id
                    WHERE v.id = ?
                    ORDER BY d.id
                    """;
            List<DetalleVentaResumen> details = new ArrayList<>();
            try (Connection connection = DatabaseConnection.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, saleId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        details.add(new DetalleVentaResumen(
                                resultSet.getInt("venta_id"),
                                resultSet.getTimestamp("fecha").toLocalDateTime(),
                                resultSet.getString("nombre_usuario"),
                                resultSet.getString("codigo"),
                                resultSet.getString("nombre"),
                                resultSet.getInt("cantidad"),
                                resultSet.getBigDecimal("precio_unitario"),
                                resultSet.getBigDecimal("subtotal"),
                                resultSet.getBigDecimal("total")
                        ));
                    }
                }
            }
            return details;
        }

        public long countToday() throws SQLException {
            return scalarLong("SELECT COUNT(*) FROM ventas WHERE fecha >= CURRENT_DATE()");
        }

        public BigDecimal totalToday() throws SQLException {
            String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE fecha >= CURRENT_DATE()";
            try (Connection connection = DatabaseConnection.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBigDecimal(1);
            }
        }

        private long scalarLong(String sql) throws SQLException {
            try (Connection connection = DatabaseConnection.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }

    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            statement.setObject(index + 1, parameters.get(index));
        }
    }

    public Venta save(Venta venta) throws SQLException {
        String sql = """
                INSERT INTO ventas (total, usuario_id)
                VALUES (?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setBigDecimal(1, venta.getTotal());
            statement.setInt(2, venta.getUsuarioId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se pudo obtener el ID de la venta creada.");
                }
                venta.setId(keys.getInt(1));
            }
        }
        return venta;
    }

    public Venta registerSale(Venta venta, List<DetalleVenta> details) throws SQLException {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un producto.");
        }

        String lockProductSql = """
                SELECT precio, stock
                FROM productos
                WHERE id = ?
                FOR UPDATE
                """;
        String insertSaleSql = """
                INSERT INTO ventas (total, usuario_id)
                VALUES (?, ?)
                """;
        String insertDetailSql = """
                INSERT INTO detalle_ventas
                    (venta_id, producto_id, cantidad, precio_unitario, subtotal)
                VALUES (?, ?, ?, ?, ?)
                """;
        String updateStockSql = """
                UPDATE productos
                SET stock = stock - ?
                WHERE id = ? AND stock >= ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal total = BigDecimal.ZERO;
                try (PreparedStatement lockProduct = connection.prepareStatement(lockProductSql);
                        PreparedStatement insertSale = connection.prepareStatement(
                                insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (DetalleVenta detail : details) {
                        lockProduct.setInt(1, detail.getProductoId());
                        try (ResultSet product = lockProduct.executeQuery()) {
                            if (!product.next()) {
                                throw new IllegalArgumentException("Uno de los productos ya no existe.");
                            }
                            BigDecimal price = product.getBigDecimal("precio");
                            int stock = product.getInt("stock");
                            if (stock < detail.getCantidad()) {
                                throw new IllegalStateException("Stock insuficiente para el producto seleccionado.");
                            }
                            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(detail.getCantidad()));
                            detail.setPrecioUnitario(price);
                            detail.setSubtotal(subtotal);
                            total = total.add(subtotal);
                        }
                    }

                    insertSale.setBigDecimal(1, total);
                    insertSale.setInt(2, venta.getUsuarioId());
                    insertSale.executeUpdate();
                    try (ResultSet keys = insertSale.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se pudo obtener el número de venta.");
                        }
                        venta.setId(keys.getInt(1));
                    }
                }

                try (PreparedStatement insertDetail = connection.prepareStatement(insertDetailSql);
                        PreparedStatement updateStock = connection.prepareStatement(updateStockSql)) {
                    for (DetalleVenta detail : details) {
                        insertDetail.setInt(1, venta.getId());
                        insertDetail.setInt(2, detail.getProductoId());
                        insertDetail.setInt(3, detail.getCantidad());
                        insertDetail.setBigDecimal(4, detail.getPrecioUnitario());
                        insertDetail.setBigDecimal(5, detail.getSubtotal());
                        insertDetail.executeUpdate();

                        updateStock.setInt(1, detail.getCantidad());
                        updateStock.setInt(2, detail.getProductoId());
                        updateStock.setInt(3, detail.getCantidad());
                        if (updateStock.executeUpdate() != 1) {
                            throw new IllegalStateException("El stock cambió durante la venta.");
                        }
                    }
                }

                venta.setTotal(details.stream()
                        .map(DetalleVenta::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
                venta.setFecha(LocalDateTime.now());
                connection.commit();
                return venta;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private Venta map(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("fecha");
        return new Venta(
                resultSet.getInt("id"),
                timestamp.toLocalDateTime(),
                resultSet.getBigDecimal("total"),
                resultSet.getInt("usuario_id")
        );
    }
}
