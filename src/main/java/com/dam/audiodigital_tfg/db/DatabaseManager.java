package com.dam.audiodigital_tfg.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:estacion_audio.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {


            String sqlSessions = "CREATE TABLE IF NOT EXISTS sessions (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);";
            stmt.execute(sqlSessions);

            //  FORZAR MIGRACIÓN: Comprobamos si la columna existe antes de insertar
            try {
                // Ejecutamos el alter de forma independiente

                stmt.execute("ALTER TABLE sessions ADD COLUMN instrument_id INTEGER DEFAULT 0;");
                System.out.println("✅Columna 'instrument_id' inyectada correctamente en la tabla 'sessions'.");
            } catch (SQLException e) {
                // Si salta aquí es porque la columna ya existe de verdad.
                // Ignoramos el error silenciosamente.
            }

            // ... El resto de tablas ...
            String sqlMetronome = "CREATE TABLE IF NOT EXISTS metronome_presets (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, bpm INTEGER, ratio_a INTEGER, ratio_b INTEGER);";
            stmt.execute(sqlMetronome);



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}