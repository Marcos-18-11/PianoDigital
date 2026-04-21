package com.dam.audiodigital_tfg;

import com.dam.audiodigital_tfg.audio.MetronomeEngine;
import com.dam.audiodigital_tfg.audio.MidiEngine;
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

    // NUEVO: Método para inyectar el motor principal desde VirtualKeyboard
    public void setMidiEngine(MidiEngine midiEngine) {
        // Creamos el metrónomo dándole el canal de percusión
        this.engine = new MetronomeEngine(midiEngine.getPercussionChannel());
    }

    @FXML
    public void initialize() {
        bpmSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int currentBpm = newValue.intValue();
            bpmLabel.setText("BPM: " + currentBpm);
            if (engine != null) {
                engine.setBpm(currentBpm);
            }
        });
    }

    @FXML
    private void togglePlay() {
        if (engine == null) return; // Seguridad extra

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
    public void stopEngine() {
        if (engine != null) {
            engine.stop();
        }
    }

}