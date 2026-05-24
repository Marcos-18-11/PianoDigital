package com.dam.audiodigital_tfg.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DrumKitDAO {

    // guardar kit completo
    public void saveCustomKit(String kitName, int[] midiNotes, String[] padLabels) {
        // Usar transacciones
        String insertKitSQL = "INSERT INTO drum_kits (name) VALUES (?)";
        String insertMappingSQL = "INSERT INTO drum_mappings (kit_id, pad_index, midi_note, label) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement pstmtKit = conn.prepareStatement(insertKitSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmtKit.setString(1, kitName);
                pstmtKit.executeUpdate();

                // Recuperamos el id
                ResultSet rs = pstmtKit.getGeneratedKeys();
                int kitId = -1;
                if (rs.next()) {
                    kitId = rs.getInt(1);
                }


                if (kitId != -1) {
                    try (PreparedStatement pstmtMapping = conn.prepareStatement(insertMappingSQL)) {
                        for (int i = 0; i < midiNotes.length; i++) {
                            pstmtMapping.setInt(1, kitId);
                            pstmtMapping.setInt(2, i);
                            pstmtMapping.setInt(3, midiNotes[i]);
                            pstmtMapping.setString(4, padLabels[i]);

                            pstmtMapping.addBatch(); // Lo añadimos a la cola
                        }
                        pstmtMapping.executeBatch(); // Ejecutamos de golpe
                    }
                }

                // Confirmamos la transacción
                conn.commit();
                System.out.println("Kit '" + kitName + "' guardado con éxito en la Base de Datos.");

            } catch (SQLException e) {
                conn.rollback(); // Si algo explota, deshacemos los cambios
                System.err.println("Error al guardar el Kit. Se ha deshecho la transacción.");
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true); // Restauramos el comportamiento por defecto
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}