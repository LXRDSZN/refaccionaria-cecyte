package com.mycompany.refaccionaria.automotriz.dao;

import com.mycompany.refaccionaria.automotriz.config.DatabaseConnection;
import com.mycompany.refaccionaria.automotriz.model.Rol;
import com.mycompany.refaccionaria.automotriz.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {

    private static final String BASE_SELECT = """
            SELECT id, nombre_usuario, password_hash, rol
            FROM usuarios
            """;

    public Optional<Usuario> findByUsername(String nombreUsuario) throws SQLException {
        String sql = BASE_SELECT + "WHERE nombre_usuario = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombreUsuario);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    public Optional<Usuario> findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    public List<Usuario> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY nombre_usuario";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                usuarios.add(map(resultSet));
            }
        }
        return usuarios;
    }

    public long count() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM usuarios");
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    public long countAdmins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN'";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    public Usuario save(Usuario usuario) throws SQLException {
        String sql = """
                INSERT INTO usuarios (nombre_usuario, password_hash, rol)
                VALUES (?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, usuario.getNombreUsuario());
            statement.setString(2, usuario.getPasswordHash());
            statement.setString(3, usuario.getRol().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se pudo obtener el ID del usuario creado.");
                }
                usuario.setId(keys.getInt(1));
            }
        }
        return usuario;
    }

    public void updateIdentityAndRole(Usuario usuario) throws SQLException {
        String sql = """
                UPDATE usuarios
                SET nombre_usuario = ?, rol = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usuario.getNombreUsuario());
            statement.setString(2, usuario.getRol().name());
            statement.setInt(3, usuario.getId());
            statement.executeUpdate();
        }
    }

    public void updatePassword(int id, String passwordHash) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    private Usuario map(ResultSet resultSet) throws SQLException {
        return new Usuario(
                resultSet.getInt("id"),
                resultSet.getString("nombre_usuario"),
                resultSet.getString("password_hash"),
                Rol.valueOf(resultSet.getString("rol"))
        );
    }
}
