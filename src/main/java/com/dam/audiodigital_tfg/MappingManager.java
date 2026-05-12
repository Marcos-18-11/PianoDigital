package com.dam.audiodigital_tfg; // Ajusta el paquete al tuyo

import com.dam.audiodigital_tfg.db.MappingDAO;
import javafx.scene.input.KeyCode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MappingManager {
    private MappingDAO mappingDAO=new MappingDAO();
    public MappingManager(){
        mappingDAO.loadMappings(pcMappings,midiMappings);
    }

    // Diccionarios de mapeo
    // Guarda: "SPACE" -> "ACTION_REC"
    private Map<String, String> pcMappings = new HashMap<>();

    // Guarda: 36 (Nota MIDI) -> "ACTION_PAD_KICK"
    private Map<Integer, String> midiMappings = new HashMap<>();

    // Lista negra de teclas intocables (para que el usuario no rompa el PC)
    private final Set<KeyCode> blacklist = Set.of(
            KeyCode.ESCAPE, KeyCode.WINDOWS, KeyCode.COMMAND, KeyCode.TAB,
            KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4, KeyCode.F5,
            KeyCode.F6, KeyCode.F7, KeyCode.F8, KeyCode.F9, KeyCode.F10, KeyCode.F11, KeyCode.F12
    );

    // Variables del "Learn Mode" (Modo de Aprendizaje)
    private boolean isLearnMode = false;
    private String actionWaitingForMap = null; // Ej: "ACTION_REC"

    // --- CONTROL DEL MODO APRENDIZAJE ---

    public void setLearnMode(boolean active) {
        this.isLearnMode = active;
        if (!active) {
            actionWaitingForMap = null; // Si apagamos el modo, cancelamos esperas
        }
    }

    public boolean isLearnMode() {
        return isLearnMode;
    }

    public void setWaitingAction(String actionId) {
        this.actionWaitingForMap = actionId;
        System.out.println("🎧 Esperando entrada (PC o MIDI) para vincular: " + actionId);
    }

    public String getWaitingAction() {
        return actionWaitingForMap;
    }

    // --- REGISTRAR MAPEOS (Cuando el usuario pulsa la tecla) ---

    public boolean mapPcKey(KeyCode code) {
        if (blacklist.contains(code)) {
            System.out.println("⚠️ Tecla reservada por el sistema. Usa otra.");
            return false;
        }
        if (actionWaitingForMap != null) {
            pcMappings.put(code.toString(), actionWaitingForMap);

            mappingDAO.saveMapping("PC", code.toString(), actionWaitingForMap);
            System.out.println("✅ Mapeado: Tecla [" + code + "] -> Acción [" + actionWaitingForMap + "]");
            actionWaitingForMap = null; // Limpiamos la espera
            return true;
        }
        return false;
    }

    public boolean mapMidiKey(int noteNumber) {
        if (actionWaitingForMap != null) {
            midiMappings.put(noteNumber, actionWaitingForMap);
            mappingDAO.saveMapping("MIDI",String.valueOf(noteNumber),actionWaitingForMap);
            System.out.println("✅ Mapeado: Pad/Nota MIDI [" + noteNumber + "] -> Acción [" + actionWaitingForMap + "]");
            actionWaitingForMap = null; // Limpiamos la espera
            return true;
        }
        return false;
    }

    // --- EJECUTAR MAPEOS (Cuando el usuario está tocando normal) ---

    public String getActionForPcKey(KeyCode code) {
        return pcMappings.get(code.toString());
    }

    public String getActionForMidiKey(int noteNumber) {
        return midiMappings.get(noteNumber);
    }
}