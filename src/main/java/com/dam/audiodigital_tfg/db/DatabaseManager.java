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
            // Aquí ejecutarías los CREATE TABLE mencionados arriba
            String sqlMetronome = "CREATE TABLE IF NOT EXISTS metronome_presets (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, bpm INTEGER, ratio_a INTEGER, ratio_b INTEGER);";
            stmt.execute(sqlMetronome);

            String sqlKits = "CREATE TABLE IF NOT EXISTS drum_kits (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);";
            stmt.execute(sqlKits);

            String sqlMappings = "CREATE TABLE IF NOT EXISTS drum_mappings (id INTEGER PRIMARY KEY AUTOINCREMENT, kit_id INTEGER, pad_index INTEGER, midi_note INTEGER, label TEXT);";
            stmt.execute(sqlMappings);

            System.out.println("Base de datos SQLite inicializada.");

            // Tabla principal de la sesión
            String sqlSessions = "CREATE TABLE IF NOT EXISTS sessions (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);";
            stmt.execute(sqlSessions);

            // Tabla para guardar cada nota individual (El "Piano Roll")
            String sqlEvents = "CREATE TABLE IF NOT EXISTS session_events (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "session_id INTEGER, " +
                    "timestamp_ms LONG, " +
                    "note INTEGER, " +
                    "velocity INTEGER, " +
                    "duration_ms INTEGER, " +
                    "is_drum BOOLEAN, " +
                    "channel INTEGER DEFAULT 0, " + // <-- ESTO ES EL PUNTO 1
                    "FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE);";
            stmt.execute(sqlEvents);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}