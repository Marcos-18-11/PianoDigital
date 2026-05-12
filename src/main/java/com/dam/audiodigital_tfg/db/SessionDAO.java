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

    // Clase auxiliar para guardar el ID y el Nombre juntos
    public static class SessionInfo {
        public int id;
        public String name;
        public SessionInfo(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; } // Lo que verá el usuario
    }

    // Método para obtener todas las sesiones
    public java.util.List<SessionInfo> getAllSessions() {
        java.util.List<SessionInfo> list = new java.util.ArrayList<>();
        String sql = "SELECT id, name FROM sessions ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new SessionInfo(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveSession(String sessionName, List<RecordedNote> notes) {
        String insertSessionSQL = "INSERT INTO sessions (name) VALUES (?)";
        String insertEventSQL = "INSERT INTO session_events (session_id, timestamp_ms, note, velocity, duration_ms, is_drum) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Iniciamos transacción

            try (PreparedStatement pstmtSess = conn.prepareStatement(insertSessionSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmtSess.setString(1, sessionName);
                pstmtSess.executeUpdate();

                ResultSet rs = pstmtSess.getGeneratedKeys();
                if (rs.next()) {
                    int sessionId = rs.getInt(1);

                    try (PreparedStatement pstmtNote = conn.prepareStatement(insertEventSQL)) {
                        for (RecordedNote note : notes) {
                            pstmtNote.setInt(1, sessionId);
                            pstmtNote.setLong(2, note.timestampMs);
                            pstmtNote.setInt(3, note.note);
                            pstmtNote.setInt(4, note.velocity);
                            pstmtNote.setInt(5, note.durationMs);
                            pstmtNote.setBoolean(6, note.isDrum);
                            pstmtNote.addBatch();
                        }
                        pstmtNote.executeBatch();
                    }
                }
                conn.commit();
                System.out.println("✅ Sesión '" + sessionName + "' guardada con " + notes.size() + " notas.");
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Método para borrar una sesión y sus notas asociadas
    public void deleteSession(int sessionId) {
        String sql = "DELETE FROM sessions WHERE id = ?";
        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            int filasBorradas = pstmt.executeUpdate();

            if (filasBorradas > 0) {
                System.out.println("✅ Sesión con ID " + sessionId + " borrada correctamente.");
            }
        } catch (java.sql.SQLException e) {
            System.err.println("❌ Error al borrar la sesión: " + e.getMessage());
        }
    }

}