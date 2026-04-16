package com.elearning.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC connection manager (singleton).
 *
 * Config priority:
 * 1) JVM args: -Ddb.url=... -Ddb.user=... -Ddb.password=...
 * 2) Environment variables: DB_URL / DB_USER / DB_PASSWORD
 * 3) Built-in defaults for local MySQL.
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/eduverse-java?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private static String resolveConfig(String propertyKey, String envKey, String fallback) {
        String systemValue = System.getProperty(propertyKey);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return fallback;
    }

    private static String getDbUrl() {
        return resolveConfig("db.url", "DB_URL", DEFAULT_URL);
    }

    private static String getDbUser() {
        return resolveConfig("db.user", "DB_USER", DEFAULT_USER);
    }

    private static String getDbPassword() {
        return resolveConfig("db.password", "DB_PASSWORD", DEFAULT_PASSWORD);
    }

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
            System.out.println("MySQL connection established.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found (mysql-connector-j).", e);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to MySQL: " + e.getMessage(), e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Reconnection error: " + e.getMessage(), e);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("MySQL connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Close connection error: " + e.getMessage());
        }
    }
}
