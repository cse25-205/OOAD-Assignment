package com.bank.dao;

import com.bank.util.BankException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Owns the JDBC connection settings and the schema. Every DAO call runs through execute(). */
public final class Database {
    /** File-based H2 database stored in ./data so records survive application restarts. */
    public static final String DEFAULT_URL = "jdbc:h2:./data/bank";

    private static String url = DEFAULT_URL;

    private Database() { }

    /** Points the application at a different database (tests use an in-memory one). */
    public static void configure(String jdbcUrl) { url = jdbcUrl; }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, "sa", "");
    }

    /** Creates the tables from src/main/resources/schema.sql (safe to run on every start-up). */
    public static void init() {
        List<String> statements = readSchema();
        execute(conn -> {
            try (Statement st = conn.createStatement()) {
                for (String sql : statements) {
                    st.execute(sql);
                }
            }
            return null;
        });
    }

    private static List<String> readSchema() {
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new BankException("schema.sql was not found on the classpath.");
            }
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            StringBuilder cleaned = new StringBuilder();
            for (String line : text.split("\\R")) {
                int comment = line.indexOf("--");
                cleaned.append(comment >= 0 ? line.substring(0, comment) : line).append('\n');
            }
            List<String> statements = new ArrayList<>();
            for (String part : cleaned.toString().split(";")) {
                if (!part.isBlank()) {
                    statements.add(part.trim());
                }
            }
            return statements;
        } catch (IOException e) {
            throw new BankException("Could not read schema.sql: " + e.getMessage(), e);
        }
    }

    /** Wipes everything and recreates the schema (used by tests). */
    public static void reset() {
        execute(conn -> {
            try (Statement st = conn.createStatement()) {
                st.execute("DROP ALL OBJECTS");
            }
            return null;
        });
        init();
    }

    @FunctionalInterface
    public interface SqlWork<T> {
        T apply(Connection connection) throws SQLException;
    }

    /** Runs the work in ONE transaction: it commits fully or rolls back fully (atomic). */
    public static <T> T execute(SqlWork<T> work) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = work.apply(conn);
                conn.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }
}
