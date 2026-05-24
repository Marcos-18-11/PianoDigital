package com.dam.audiodigital_tfg.db;

import com.dam.audiodigital_tfg.RecordedNote;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    public List<RecordedNote> getNotesBySession(int sessionId) {
        List<RecordedNote> notes = new ArrayList<>();
        String sql = "SELECT * FROM session_events WHERE session_id = ? ORDER BY timestamp_ms ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notes.add(new RecordedNote(
                        rs.getLong("timestamp_ms"),
                        rs.getInt("note"),
                        rs.getInt("velocity"),
                        rs.getInt("duration_ms"),
                        rs.getBoolean("is_drum")
                        // Aquí podrías añadir el canal si lo necesitas
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    //  CLASE AUXILIAR
    public static class SessionInfo {
        public int id;
        public String name;
        public int instrumentId; // <-- Variable añadida aquí


        public SessionInfo(int id, String name, int instrumentId) {
            this.id = id;
            this.name = name;
            this.instrumentId = instrumentId;
        }

        @Override
        public String toString() {
            return name; // Lo que verá el usuario
        }
    }

    // Método para obtener todas las sesiones
    public java.util.List<SessionInfo> getAllSessions() {
        java.util.List<SessionInfo> sessions = new java.util.ArrayList<>();
        // Añadimos instrument_id al SELECT
        String sql = "SELECT id, name, instrument_id FROM sessions ORDER BY created_at DESC";

        try (java.sql.Connection conn = com.dam.audiodigital_tfg.db.DatabaseManager.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Instancia
                sessions.add(new SessionInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("instrument_id") // Recuperamos el instrumento
                ));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    public void saveSession(String sessionName, int instrumentId, java.util.List<com.dam.audiodigital_tfg.RecordedNote> notes) {
        // Actualizamos el INSERT para incluir la columna instrument_id
        String sqlInsertSession = "INSERT INTO sessions(name, instrument_id) VALUES(?, ?)";

        try (java.sql.Connection conn = com.dam.audiodigital_tfg.db.DatabaseManager.getConnection();
             java.sql.PreparedStatement pstmtSession = conn.prepareStatement(sqlInsertSession, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            pstmtSession.setString(1, sessionName);
            pstmtSession.setInt(2, instrumentId); // Guardamos el instrumento en la BD
            pstmtSession.executeUpdate();

            java.sql.ResultSet rs = pstmtSession.getGeneratedKeys();
            int sessionId = -1;
            if (rs.next()) {
                sessionId = rs.getInt(1);
            }

            // -------------------------------------------------------------
            // CÓDIGO ORIGINAL PARA GUARDAR LAS NOTAS EN session_events
            // -------------------------------------------------------------
            String sqlInsertEvent = "INSERT INTO session_events(session_id, timestamp_ms, note, velocity, duration_ms, is_drum, channel) VALUES(?, ?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement pstmtEvent = conn.prepareStatement(sqlInsertEvent)) {
                for (com.dam.audiodigital_tfg.RecordedNote note : notes) {
                    pstmtEvent.setInt(1, sessionId);
                    pstmtEvent.setLong(2, note.timestampMs);
                    pstmtEvent.setInt(3, note.note);
                    pstmtEvent.setInt(4, note.velocity);
                    pstmtEvent.setInt(5, note.durationMs);
                    pstmtEvent.setBoolean(6, note.isDrum);
                    pstmtEvent.setInt(7, note.isDrum ? 9 : 0);
                    pstmtEvent.addBatch();
                }
                pstmtEvent.executeBatch();
            }

            System.out.println("Sesión guardada ");

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para borrar una sesión y sus notas asociadas
    public void deleteSession(int sessionId) {
        String sql = "DELETE FROM sessions WHERE id = ?";
        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            int filasBorradas = ps.executeUpdate();

            if (filasBorradas > 0) {
                System.out.println("Sesión con ID " + sessionId + " borrada correctamente.");
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al borrar la sesión: " + e.getMessage());
        }
    }
}