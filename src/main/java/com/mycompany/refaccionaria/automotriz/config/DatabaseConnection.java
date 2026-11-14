package com.mycompany.refaccionaria.automotriz.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:mariadb://localhost:3306/refaccionaria_automotriz";
    private static final String URL = configuredUrl();
    private static final String USER = setting("db.user", "DB_USER", "root");
    private static final String PASSWORD = setting("db.password", "DB_PASSWORD", "");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getUrl() {
        return URL;
    }

    private static String configuredUrl() {
        String explicitUrl = setting("db.url", "DB_URL", "");
        if (!explicitUrl.isBlank()) {
            return explicitUrl;
        }

        String socket = setting("db.socket", "DB_SOCKET", "");
        if (!socket.isBlank()) {
            return DEFAULT_URL + "?localSocket=" + socket;
        }

        return DEFAULT_URL;
    }

    private static String setting(String property, String environmentVariable, String defaultValue) {
        String configuredProperty = System.getProperty(property);
        if (configuredProperty != null && !configuredProperty.isBlank()) {
            return configuredProperty;
        }

        String configuredEnvironment = System.getenv(environmentVariable);
        if (configuredEnvironment != null && !configuredEnvironment.isBlank()) {
            return configuredEnvironment;
        }

        return defaultValue;
    }
}
