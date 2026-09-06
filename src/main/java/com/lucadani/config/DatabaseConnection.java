package com.lucadani.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String JDBC_URL = dotenv.get("SUPABASE_URL");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }
}
