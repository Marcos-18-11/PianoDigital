package com.dam.audiodigital_tfg.db;

import com.dam.audiodigital_tfg.MappingManager;

import java.sql.*;
import java.util.Map;


public class MappingDAO {

    public MappingDAO(){
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_mappings (" +
                "input_type TEXT NOT NULL, " +
                "input_value TEXT NOT NULL, " +
                "action_id TEXT NOT NULL, " +
                "PRIMARY KEY (input_type, action_id)" +
                ");";
        try(Connection conn= DatabaseManager.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
        }catch (SQLException e) {
           e.printStackTrace();
        }
    }
    public void saveMapping(String inputType, String inputValue, String actionId){
    String sql="INSERT OR REPLACE INTO user_mappings (input_type, input_value, action_id)VALUES (?,?,?)";
    try(Connection conn = DatabaseManager.getConnection()){
        PreparedStatement pstmt=conn.prepareStatement(sql);
        pstmt.setString(1, inputType);
        pstmt.setString(2, inputValue);
        pstmt.setString(3, actionId);
        pstmt.executeUpdate();
    } catch (SQLException e){
        e.printStackTrace();
        System.err.println("Error al guardar el mapeo: " + e.getMessage());
    }
    }
    public void loadMappings(Map<String, String> pcMappings, Map<Integer, String> midiMappings){
    String sql ="SELECT * FROM user_mappings";
    try(Connection conn = DatabaseManager.getConnection();
    Statement stmt= conn.createStatement();
    ResultSet rs = stmt.executeQuery(sql)){
        while (rs.next()){
            String type = rs.getString("input_type");
            String value = rs.getString("input_value");
            String action = rs.getString("action_id");

            if ("PC".equals(type)) {
                pcMappings.put(value, action);
            } else if ("MIDI".equals(type)) {
                midiMappings.put(Integer.parseInt(value), action);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al cargar mapeos: " + e.getMessage());
    }
    }
}
