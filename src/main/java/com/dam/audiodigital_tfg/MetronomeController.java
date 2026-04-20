package com.dam.audiodigital_tfg;

import com.dam.audiodigital_tfg.audio.MetronomeEngine;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class MetronomeController {

    @FXML private Label bpmLabel;
    @FXML private Slider bpmSlider;
    @FXML private Button playButton;

    private MetronomeEngine engine;
    private boolean isPlaying = false;

    @FXML
    public void initialize() {
        engine = new MetronomeEngine();

        // Conectar el slider con la etiqueta y el motor
        bpmSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int currentBpm = newValue.intValue();
            bpmLabel.setText("BPM: " + currentBpm);
            engine.setBpm(currentBpm);
        });
    }

    @FXML
    private void togglePlay() {
        if (isPlaying) {
            engine.stop();
            playButton.setText("▶ PLAY");
            playButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        } else {
            engine.start();
            playButton.setText("⏸ STOP");
            playButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        }
        isPlaying = !isPlaying;
    }
}