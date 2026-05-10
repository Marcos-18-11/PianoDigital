package com.dam.audiodigital_tfg;

import com.dam.audiodigital_tfg.audio.MetronomeEngine;
import com.dam.audiodigital_tfg.audio.MidiEngine;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class MetronomeController {

    @FXML private Label bpmLabel;
    @FXML private Slider bpmSlider;
    @FXML private Button playButton;
    @FXML private ComboBox<String> polyComboBox;

    private MetronomeEngine engine;
    private boolean isPlaying = false;

    // Inyectamos el motor principal y creamos el del metrónomo
    public void setMidiEngine(MidiEngine midiEngine) {
        this.engine = new MetronomeEngine(midiEngine.getPercussionChannel());
    }

    @FXML
    public void initialize() {
        // 1. Configurar el Slider de BPM
        bpmSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int currentBpm = newValue.intValue();
            bpmLabel.setText("BPM: " + currentBpm);
            if (engine != null) {
                engine.setBpm(currentBpm);
            }
        });

        // 2. Configurar el ComboBox de Polirritmia
        polyComboBox.getItems().addAll(
                "Ninguno",
                "2 (Dosillo)",
                "3 (Tresillo)",
                "4 (Cuatrillo)",
                "5 (Quintillo)",
                "6 (Seisillo)"
        );
        polyComboBox.setValue("Ninguno");

        // 3. Lógica al cambiar el Polirritmo
        polyComboBox.setOnAction(event -> {
            if (engine != null) {
                String selected = polyComboBox.getValue();
                int secondaryRhythm = 0; // 0 significa desactivado

                // Extraemos el número de la selección
                if (selected.startsWith("2")) secondaryRhythm = 2;
                else if (selected.startsWith("3")) secondaryRhythm = 3;
                else if (selected.startsWith("4")) secondaryRhythm = 4;
                else if (selected.startsWith("5")) secondaryRhythm = 5;
                else if (selected.startsWith("6")) secondaryRhythm = 6;

                engine.setSecondaryRhythm(secondaryRhythm);

                // Si ya estaba sonando, lo reiniciamos para que los hilos se sincronicen
                if (isPlaying) {
                    engine.stop();
                    try { Thread.sleep(50); } catch (InterruptedException e) {} // Pequeña pausa
                    engine.start();
                }
            }
        });
    }

    @FXML
    private void togglePlay() {
        if (engine == null) return;

        if (isPlaying) {
            engine.stop();
            playButton.setText("▶ PLAY");
            playButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            engine.start();
            playButton.setText("⏸ STOP");
            playButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        }
        isPlaying = !isPlaying;
    }

    public void stopEngine() {

        if (engine != null) {
            engine.stop();
        }

        isPlaying = false;
    }

}