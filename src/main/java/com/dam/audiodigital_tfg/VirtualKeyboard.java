package com.dam.audiodigital_tfg;

import com.dam.audiodigital_tfg.audio.MidiEngine;
import com.dam.audiodigital_tfg.db.SessionDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class VirtualKeyboard extends Application {

    private MidiEngine midiEngine;
    private MetronomeController metroController;

    private int baseMidiNote = 60;

    private static final String WHITE_KEY_STYLE =
            "-fx-background-color: white; -fx-border-color: black; -fx-cursor: hand;";

    private static final String BLACK_KEY_STYLE =
            "-fx-background-color: black; -fx-text-fill: white; -fx-cursor: hand;";

    private static final Map<Integer, String> OCTAVE_STRUCTURE = new HashMap<>();

    private MappingManager mappingManager = new MappingManager();


    static {
        OCTAVE_STRUCTURE.put(0, "Do");
        OCTAVE_STRUCTURE.put(1, "Do#");
        OCTAVE_STRUCTURE.put(2, "Re");
        OCTAVE_STRUCTURE.put(3, "Re#");
        OCTAVE_STRUCTURE.put(4, "Mi");
        OCTAVE_STRUCTURE.put(5, "Fa");
        OCTAVE_STRUCTURE.put(6, "Fa#");
        OCTAVE_STRUCTURE.put(7, "Sol");
        OCTAVE_STRUCTURE.put(8, "Sol#");
        OCTAVE_STRUCTURE.put(9, "La");
        OCTAVE_STRUCTURE.put(10, "La#");
        OCTAVE_STRUCTURE.put(11, "Si");
    }

    @Override
    public void init() {

        com.dam.audiodigital_tfg.db.DatabaseManager.initDatabase();

        midiEngine = new MidiEngine();
        midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // =========================
        // TOP PANEL (Piano, Guitarra e Importar)
        // =========================
        VBox topPane = new VBox(10);
        topPane.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Virtual Keyboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Creamos un contenedor horizontal para poner los controles uno al lado del otro
        HBox controlsHBox = new HBox(15);
        controlsHBox.setAlignment(Pos.CENTER);

        // 1. Selector de Instrumentos
        ComboBox<String> instrumentSelector = new ComboBox<>();
        instrumentSelector.getItems().addAll(
                "Piano Acústico", "Piano Eléctrico", "Órgano", "Órgano de Iglesia",
                "Guitarra Acústica", "Guitarra Jazz", "Bajo Eléctrico", "Contrabajo",
                "Viola", "Orquesta", "Coro", "Trompeta", "Cuerno Francés",
                "Saxofón", "Flauta", "Campanas Tubulares", "Sitar",
                "Synth Lead", "Warm Pad", "Brightness"
        );
        instrumentSelector.setValue("Piano");
        instrumentSelector.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");


        // 2. Switch conectado a tus constantes de MidiEngine
        instrumentSelector.setOnAction(event -> {
            String selected = instrumentSelector.getValue();
            switch (selected) {
                case "Piano Acústico":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO);
                    break;
                case "Piano Eléctrico":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO_ELECTRIC);
                    break;
                case "Órgano":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_ORGAN);
                    break;
                case "Órgano de Iglesia":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_CHURCH_ORGAN);
                    break;
                case "Guitarra Acústica":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_GUITAR);
                    break;
                case "Guitarra Jazz":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_JAZZ_GUITAR);
                    break;
                case "Bajo Eléctrico":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_GELECTRIC_BASS);
                    break;
                case "Contrabajo":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_CONTRABASS);
                    break;
                case "Viola":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_VIOLA);
                    break;
                case "Orquesta":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_ORCHESTA);
                    break;
                case "Coro":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_CHOIR);
                    break;
                case "Trompeta":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_TRUMPET);
                    break;
                case "Corno Francés":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_FRENCH_HORN);
                    break;
                case "Saxofón":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_SAX);
                    break;
                case "Flauta":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_FLUTE);
                    break;
                case "Campanas Tubulares":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_TUBULAR_BELLS);
                    break;
                case "Sitar":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_SITAR);
                    break;
                case "Synth Lead":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_SINTH_LEAD);
                    break;
                case "Warm Pad":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_WARM_PAD);
                    break;
                case "Brightness":
                    midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_BRIGHTNESS);
                    break;
            }
        });

        // En tu archivo Controller.java
        ToggleButton btnSaturationOn = new ToggleButton("🔥 Saturación (C++)");
        // Le damos un estilo inicial
        btnSaturationOn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand;");

        // Listener para activar/desactivar y cambiar el color
        btnSaturationOn.setOnAction(event -> {
            boolean isOn = btnSaturationOn.isSelected();
            midiEngine.setSaturationEnabled(isOn);

            if (isOn) {
                // Si está encendido, lo ponemos rojo/naranja
                btnSaturationOn.setStyle("-fx-background-color: #ff5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                // Si está apagado, vuelve a gris
                btnSaturationOn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand;");
            }
        });

        // --- 1. REVERB (Slider) ---
        // --- 1. REVERB (Slider) ---
        VBox reverbBox = new VBox(5);
        reverbBox.setAlignment(Pos.CENTER);
        Label lblRev = new Label("🌊 Reverb");
        lblRev.setStyle("-fx-font-weight: bold;");
        Slider revSlider = new Slider(0, 127, 0);
        revSlider.setPrefWidth(100);

        // 🚨 CAMBIO AQUÍ: Ahora enviamos el nivel exacto
        revSlider.valueProperty().addListener((obs, old, val) -> {
            midiEngine.setReverbLevel(val.intValue());
        });

        reverbBox.getChildren().addAll(lblRev, revSlider);

        // --- 2. SATURACIÓN C++ (Botón y Slider) ---
        VBox satBox = new VBox(5);
        satBox.setAlignment(Pos.CENTER);
        ToggleButton btnSaturation = new ToggleButton("🔥 Saturación");
        btnSaturation.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand;");

        Slider satSlider = new Slider(0, 10, 0); // Lo empezamos en 0
        satSlider.setPrefWidth(100);

        // 🚨 CAMBIO AQUÍ: Ahora enviamos la fuerza exacta (Drive)
        satSlider.valueProperty().addListener((obs, old, val) -> {
            midiEngine.setSaturationDrive(val.floatValue());

            // Efecto visual: Si subimos el slider, el botón se enciende solo
            if (val.floatValue() > 0.1) {
                btnSaturation.setSelected(true);
                btnSaturation.setStyle("-fx-background-color: #ff5722; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                btnSaturation.setSelected(false);
                btnSaturation.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
            }
        });

        // (Mantenemos el botón por si queremos apagarlo de golpe sin mover el slider)
        btnSaturation.setOnAction(event -> {
            boolean isOn = btnSaturation.isSelected();
            midiEngine.setSaturationEnabled(isOn);
            btnSaturation.setStyle(isOn ? "-fx-background-color: #ff5722; -fx-text-fill: white; -fx-font-weight: bold;" : "-fx-background-color: #555555; -fx-text-fill: white;");
        });

        satBox.getChildren().addAll(btnSaturation, satSlider);

        // --- 3. DELAY (Botón) ---
        ToggleButton btnDelay = new ToggleButton("⏱️ Delay");
        btnDelay.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand;");
        btnDelay.setOnAction(event -> {
            boolean isOn = btnDelay.isSelected();
            midiEngine.setDelayEnabled(isOn);
            btnDelay.setStyle(isOn ? "-fx-background-color: #00E676; -fx-text-fill: black; -fx-font-weight: bold;" : "-fx-background-color: #555555; -fx-text-fill: white;");
        });

        // --- 4. IMPORTAR ---
        Button importBtn = new Button("📂 Importar (.sf2)");
        importBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        importBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("SoundFont", "*.sf2"));
            java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) midiEngine.loadCustomSoundbank(selectedFile);
        });

        // 🚨 ENSAMBLAMOS TODO UNA SOLA VEZ 🚨
        controlsHBox.getChildren().clear();
        controlsHBox.getChildren().addAll(instrumentSelector, satBox, reverbBox, btnDelay, importBtn);

        // Metemos el título y la caja en el panel superior
        topPane.getChildren().addAll(titleLabel, controlsHBox);
        root.setTop(topPane);
// LEFT PANEL (Mezclador 4 Pistas con Sliders)
// =========================
        VBox leftPane = new VBox(15); // <-- Aquí se define el leftPane
        leftPane.setAlignment(Pos.TOP_CENTER);
        leftPane.setPadding(new Insets(20));
        leftPane.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 1 0 0; -fx-background-color: #2b2b2b;");

        Label mixerLabel = new Label("MIXER");
        mixerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        leftPane.getChildren().add(mixerLabel);

// Mapa para que el hardware sepa qué slider mover
        Map<Integer, Slider> vSliders = new HashMap<>();

        for (int i = 0; i < 4; i++) {
            int channelIdx = i;
            VBox channelRack = new VBox(8); // Aumentamos un poco el espacio
            channelRack.setAlignment(Pos.CENTER);
            channelRack.setPadding(new Insets(10));
            channelRack.setStyle("-fx-background-color: #3c3f41; -fx-background-radius: 8; -fx-border-color: #555;");

            Label lblTrack = new Label("PISTA " + (i + 1));
            lblTrack.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold; -fx-font-size: 11px;");

            // 1. Slider de Volumen
            // 1. Slider de Volumen
            Slider volSlider = new Slider(0, 127, 100);
            volSlider.setOrientation(Orientation.VERTICAL);
            volSlider.setPrefHeight(120);
            int ccMap = (i == 0) ? 82 : (i == 1) ? 83 : (i == 2) ? 85 : 17;
            vSliders.put(ccMap, volSlider);

// 🚨 ARREGLO: Abrimos llaves { } para que haga las dos cosas
            volSlider.valueProperty().addListener((obs, old, val) -> {
                midiEngine.setChannelVolume(channelIdx, val.intValue());
                midiEngine.setChannelVolume(9, val.intValue()); // Usamos 'val' y está dentro de las llaves
            });


            // 2. Selector de Sesión
            ComboBox<com.dam.audiodigital_tfg.db.SessionDAO.SessionInfo> sessionSelector = new ComboBox<>();
            sessionSelector.setPromptText("Cargar...");
            sessionSelector.setPrefWidth(100);
            sessionSelector.setStyle("-fx-font-size: 10px;");

            // Contenedor temporal para las notas cargadas en este canal específico
            final java.util.List<com.dam.audiodigital_tfg.RecordedNote>[] loadedNotes = new java.util.List[1];

            sessionSelector.setOnShowing(e -> {
                sessionSelector.getItems().setAll(new com.dam.audiodigital_tfg.db.SessionDAO().getAllSessions());
            });

            // 3. Botón de PLAY para este canal
            Button playTrackBtn = new Button("▶");
            playTrackBtn.setDisable(true); // Deshabilitado hasta que carguemos algo
            playTrackBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
            playTrackBtn.setPrefWidth(100);

            // Cuando seleccionamos una sesión en el ComboBox...
            sessionSelector.setOnAction(e -> {
                var selected = sessionSelector.getValue();
                if (selected != null) {
                    loadedNotes[0] = new com.dam.audiodigital_tfg.db.SessionDAO().getNotesBySession(selected.id);
                    playTrackBtn.setDisable(false); // Ya hay notas, habilitamos el Play
                    playTrackBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;"); // Azul: "Listo para sonar"
                }
            });

            // Acción del botón Play
            playTrackBtn.setOnAction(e -> {
                if (loadedNotes[0] != null && !loadedNotes[0].isEmpty()) {
                    midiEngine.playSession(loadedNotes[0], channelIdx);

                    // Efecto visual rápido de reproducción
                    playTrackBtn.setText("⏳");
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> playTrackBtn.setText("▶"));
                        }
                    }, 1000);
                }
            });

            // Añadimos todo al rack del canal (Orden: Nombre -> Slider -> Combo -> Play)
            channelRack.getChildren().addAll(lblTrack, volSlider, sessionSelector, playTrackBtn);
            leftPane.getChildren().add(channelRack);
        }

// Conectamos el listener del hardware
        midiEngine.setControlChangeListener((cc, value) -> {
            if (vSliders.containsKey(cc)) {
                vSliders.get(cc).setValue(value);
            }
        });

        root.setLeft(leftPane); // <-- Aquí lo metes en el BorderPane

// 🚨 CONEXIÓN HARDWARE -> SOFTWARE 🚨
        midiEngine.setControlChangeListener((cc, value) -> {
            if (vSliders.containsKey(cc)) {
                vSliders.get(cc).setValue(value); // El fader físico mueve el slider virtual
            }
        });


        // =========================
        // KEYBOARD
        // =========================
        HBox keyboardContainer = new HBox(1);
        keyboardContainer.setAlignment(Pos.CENTER);
        keyboardContainer.setPadding(new Insets(20, 0, 20, 0));

        createKeyboard(keyboardContainer, 2);

        root.setCenter(keyboardContainer);

        // =========================
        // OCTAVE CONTROLS
        // =========================
        VBox octaveControls = new VBox(10);
        octaveControls.setAlignment(Pos.CENTER);
        octaveControls.setPadding(new Insets(0, 0, 0, 20));

        Label octaveLabel = new Label("Octava: C4");
        octaveLabel.setStyle("-fx-font-weight: bold;");

        Button upButton = new Button("Up");
        upButton.setPrefSize(40, 40);

        upButton.setOnAction(event -> {
            if (baseMidiNote <= 108) {
                baseMidiNote += 12;
                octaveLabel.setText("Octava: C" + (baseMidiNote / 12 - 1));
            }
        });

        Button downButton = new Button("Down");
        downButton.setPrefSize(40, 40);

        downButton.setOnAction(event -> {
            if (baseMidiNote >= 12) {
                baseMidiNote -= 12;
                octaveLabel.setText("Octava: C" + (baseMidiNote / 12 - 1));
            }
        });

        octaveControls.getChildren().addAll(upButton, octaveLabel, downButton);
        root.setRight(octaveControls);

        // =========================
        // METRONOME
        // =========================
        FXMLLoader fxmlLoader = new FXMLLoader(VirtualKeyboard.class.getResource("MetronomeView.fxml"));
        Parent metronomeRoot = fxmlLoader.load();

        metroController = fxmlLoader.getController();

        if (metroController != null) {
            metroController.setMidiEngine(midiEngine);
        }

        // ==========================================
        // ENSAMBLAMOS LAS PESTAÑAS Y EL SPLITPANE
        // ==========================================

        // Creamos el contenedor de pestañas
        javafx.scene.control.TabPane tabPane = new javafx.scene.control.TabPane();

        // Pestaña 1: El Piano
        javafx.scene.control.Tab tabPiano = new javafx.scene.control.Tab("🎹 Teclado Virtual", root);
        tabPiano.setClosable(false);

        // Pestaña 2: El Drum Pad
        javafx.scene.control.Tab tabDrums = new javafx.scene.control.Tab("🥁 Drum Pad", createDrumPad());
        tabDrums.setClosable(false);

        // Añadimos las pestañas al TabPane
        tabPane.getTabs().addAll(tabPiano, tabDrums);

        // Creamos un único SplitPane Vertical
        javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Añadimos el Metrónomo (arriba) y el TabPane (abajo)
        splitPane.getItems().addAll(metronomeRoot, tabPane);

        // Ajustamos la posición inicial de la barra separadora
        splitPane.setDividerPositions(0.3f);

        // =========================
        // SCENE (Renderizado final)
        // =========================
        Scene scene = new Scene(splitPane, 1000, 700);

        primaryStage.setTitle("Music Box DAW");
        primaryStage.setScene(scene);
        primaryStage.show();

        // =========================
        // BARRA DE TRANSPORTE (REC / STOP / MAPEO)
        // =========================
        HBox transportBar = new HBox(10);
        transportBar.setAlignment(Pos.CENTER);

        Button recBtn = new Button("🔴 REC");
        Button stopBtn = new Button("⏹ STOP");
        stopBtn.setDisable(true); // Deshabilitado hasta que grabemos

        // 1. CREAMOS EL BOTÓN DE MAPEO
        ToggleButton btnMapeo = new ToggleButton("🎧 MIDI/Key Learn");
        btnMapeo.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        recBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
        stopBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");

        // 2. LÓGICA DEL BOTÓN DE MAPEO
        btnMapeo.setOnAction(e -> {
            boolean activo = btnMapeo.isSelected();
            mappingManager.setLearnMode(activo);

            if (activo) {
                btnMapeo.setText("🛑 Escuchando...");
                btnMapeo.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                btnMapeo.setText("🎧 MIDI/Key Learn");
                btnMapeo.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        });

        // 3. LÓGICA DEL BOTÓN REC
        recBtn.setOnAction(e -> {
            if (mappingManager.isLearnMode()) {
                mappingManager.setWaitingAction("ACTION_REC");
                recBtn.setStyle("-fx-border-color: yellow; -fx-border-width: 3px; -fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                midiEngine.startRecording();
                recBtn.setDisable(true);
                stopBtn.setDisable(false);
                recBtn.setText("⏺ GRABANDO...");
            }
        });

        // 4. LÓGICA DEL BOTÓN STOP
        stopBtn.setOnAction(e -> {
            if (mappingManager.isLearnMode()) {
                mappingManager.setWaitingAction("ACTION_STOP");
                stopBtn.setStyle("-fx-border-color: yellow; -fx-border-width: 3px; -fx-background-color: #555555; -fx-text-fill: white;");
            } else {
                midiEngine.stopRecording();
                recBtn.setDisable(false);
                stopBtn.setDisable(true);
                recBtn.setText("🔴 REC");

                javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("Mi Sesión " + System.currentTimeMillis());
                dialog.setTitle("Guardar Grabación");
                dialog.setHeaderText("¡Grabación finalizada!");
                dialog.setContentText("Introduce el nombre de la sesión:");

                dialog.showAndWait().ifPresent(name -> {
                    com.dam.audiodigital_tfg.db.SessionDAO sessionDAO = new com.dam.audiodigital_tfg.db.SessionDAO();
                    sessionDAO.saveSession(name, midiEngine.getRecordedNotes());
                });
            }
        });

        // 5. AÑADIMOS LOS 3 BOTONES AL CONTENEDOR (¡Aquí está la clave!)
        transportBar.getChildren().addAll(recBtn, stopBtn, btnMapeo);

        // Añadimos la barra de transporte al topPane
        topPane.getChildren().add(transportBar);

        // 🚨 BLOQUE LISTENER CORREGIDO 🚨
        midiEngine.setActionTriggerListener(new MidiEngine.ActionTriggerListener() {
            @Override
            public void onActionTriggered(String actionId) {
                // Aquí centralizamos todas las acciones globales
                switch (actionId) {
                    case "ACTION_REC":
                        recBtn.fire(); // Dispara el botón rojo de la barra de transporte
                        break;
                    case "ACTION_STOP":
                        stopBtn.fire(); // Dispara el botón gris de la barra de transporte
                        break;
                }
            }

            @Override
            public void onMappingSuccess() {
                // Limpiamos los bordes amarillos y devolvemos el color original
                recBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-width: 0;");
                stopBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-border-width: 0;");
                System.out.println("✨ Mapeo capturado con éxito.");
            }
        });

        // Lista exclusiva para evitar la metralleta del piano
        java.util.Set<String> notasSonando = new java.util.HashSet<>();
        // 🚨 EL ÚNICO INTERCEPTOR MAESTRO DE TECLADO PC 🚨
        scene.setOnKeyPressed(event -> {
            javafx.scene.input.KeyCode code = event.getCode();

            if (mappingManager.isLearnMode()) {
                // 1. MODO APRENDER
                if (mappingManager.getWaitingAction() != null) {
                    boolean mapeado = mappingManager.mapPcKey(code);
                    if (mapeado) {
                        recBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-width: 0;");
                        stopBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-border-width: 0;");
                        System.out.println("Tecla [" + code + "] vinculada con éxito.");
                    }
                }
            } else {
                // 2. MODO NORMAL
                String accion = mappingManager.getActionForPcKey(code);

                if (accion != null) {

                    if (accion.equals("ACTION_REC")) {
                        recBtn.fire();
                    } else if (accion.equals("ACTION_STOP")) {
                        stopBtn.fire();
                    }
                    else if (accion.startsWith("DRUM_")) {
                        // BATERÍA
                        try {
                            int note = Integer.parseInt(accion.split("_")[1]);
                            if (midiEngine != null) midiEngine.playPad(note, 127);
                        } catch (Exception ex) { }
                    }
                    else if (accion.startsWith("PIANO_")) {
                        // 🚨 Barrera anti-metralleta SOLO para el piano
                        if (notasSonando.contains(accion)) {
                            return; // Si la nota ya está sonando, ignoramos
                        }
                        notasSonando.add(accion); // Marcamos que empieza a sonar

                        try {
                            int note = Integer.parseInt(accion.split("_")[1]);
                            if (midiEngine != null) midiEngine.noteOn(note, 127);
                        } catch (Exception ex) { }
                    }
                }
            }
        });

        // 🚨 INTERCEPTOR PARA SOLTAR LA TECLA (Vital para que el piano no suene infinito) 🚨
        scene.setOnKeyReleased(event -> {
            if (!mappingManager.isLearnMode()) {
                String accion = mappingManager.getActionForPcKey(event.getCode());
                if (accion != null && accion.startsWith("PIANO_")) {

                    // 🚨 Borramos la nota de la lista porque la hemos soltado
                    notasSonando.remove(accion);

                    try {
                        int note = Integer.parseInt(accion.split("_")[1]);
                        if (midiEngine != null) midiEngine.noteOff(note);
                    } catch (Exception ex) { }
                }
            }
        });

        primaryStage.setTitle("Music Box DAW");
        primaryStage.setScene(scene);
        primaryStage.show();

    }



    private void createKeyboard(HBox container, int numOctaves) {

        container.getChildren().clear();

        for (int octave = 0; octave < numOctaves; octave++) {

            for (int noteInOctave = 0; noteInOctave < 12; noteInOctave++) {

                String noteName = OCTAVE_STRUCTURE.get(noteInOctave);

                boolean isBlack = noteName.contains("#");

                Button key = new Button(noteName);

                if (isBlack) {
                    key.setStyle(BLACK_KEY_STYLE);
                    key.setPrefSize(35, 125);
                } else {
                    key.setStyle(WHITE_KEY_STYLE);
                    key.setPrefSize(50, 200);
                }

                // Calculamos la nota MIDI real sumando la nota base (C4 u otra)
                int actualMidiNote = baseMidiNote + (octave * 12) + noteInOctave;

                // Generamos el ID dinámico, por ejemplo: "PIANO_60"
                final String actionId = "PIANO_" + actualMidiNote;

                // Al hacer clic: decidimos si mapeamos o tocamos
                key.setOnMousePressed(mouseEvent -> {
                    if (mappingManager.isLearnMode()) {
                        // MODO APRENDER: Esperamos la tecla del PC y ponemos color llamativo
                        mappingManager.setWaitingAction(actionId);
                        key.setStyle("-fx-background-color: yellow; -fx-border-color: orange; -fx-border-width: 3px; -fx-cursor: hand;");
                    } else {
                        // MODO NORMAL: Tocamos la nota y cambiamos a gris como feedback
                        key.setStyle("-fx-background-color: #a0a0a0; -fx-cursor: hand;");
                        midiEngine.noteOn(actualMidiNote, 100);
                    }
                });

                // Al soltar el clic
                key.setOnMouseReleased(mouseEvent -> {
                    // Solo devolvemos el color original si NO está esperando ser mapeada (amarilla)
                    if (!mappingManager.isLearnMode() || !actionId.equals(mappingManager.getWaitingAction())) {
                        key.setStyle(isBlack ? BLACK_KEY_STYLE : WHITE_KEY_STYLE);
                    }
                    midiEngine.noteOff(actualMidiNote);
                });

                // Por si el usuario arrastra el ratón fuera del botón sin soltar el clic
                key.setOnMouseExited(mouseEvent -> {
                    if (!mappingManager.isLearnMode() || !actionId.equals(mappingManager.getWaitingAction())) {
                        key.setStyle(isBlack ? BLACK_KEY_STYLE : WHITE_KEY_STYLE);
                    }
                    midiEngine.noteOff(actualMidiNote);
                });

                container.getChildren().add(key);
            }
        }
    }

    private javafx.scene.layout.GridPane createDrumPad() {
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Mapeo estándar MIDI de Percusión (Canal 10)
        int[] drumNotes = {
                49, 51, 52, 53, // Platos
                43, 45, 47, 48, // Toms
                42, 44, 46, 55, // Hi-Hats y FX
                36, 38, 37, 39  // Kick, Snare, Rimshot, Clap
        };

        String[] drumNames = {
                "Crash", "Ride", "China", "Bell",
                "Tom H", "Tom M", "Tom L", "Tom XL",
                "HiHat C", "Pedal", "HiHat O", "Splash",
                "KICK", "SNARE", "Rim", "Clap"
        };

        int count = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                Button pad = new Button(drumNames[count]);
                pad.setPrefSize(100, 100);
                pad.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 10;");

                final int midiNote = drumNotes[count];

                // 🔥 CREAMOS UN ID DE ACCIÓN DINÁMICO (Ej: "DRUM_36")
                final String actionId = "DRUM_" + midiNote;

                // Evento al pulsar
                pad.setOnMousePressed(e -> {
                    if (mappingManager.isLearnMode()) {
                        // MODO APRENDER: El pad pide ser mapeado y se pone amarillo
                        mappingManager.setWaitingAction(actionId);
                        pad.setStyle("-fx-background-color: #333333; -fx-border-color: yellow; -fx-border-width: 3px; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-border-radius: 10;");
                    } else {
                        // MODO NORMAL: Se pone verde y suena
                        pad.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
                        // Usamos playPad para que pase por el grabador de sesiones
                        if (midiEngine != null) {
                            midiEngine.playPad(midiNote, 127);
                        }
                    }
                });

                // Evento al soltar el clic
                pad.setOnMouseReleased(e -> {
                    // Solo le quitamos el color verde si no está esperando a ser mapeado (amarillo)
                    if (!mappingManager.isLearnMode() || !actionId.equals(mappingManager.getWaitingAction())) {
                        pad.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
                    }
                });

                grid.add(pad, col, row);
                count++;
            }
        }

        // Botón para guardar el Kit actual en la Base de Datos (INTACTO)
        Button saveKitBtn = new Button("💾 Guardar Kit...");
        saveKitBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        saveKitBtn.setOnAction(e -> {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("Nuevo Kit MiniLab");
            dialog.setTitle("Guardar Drum Kit");
            dialog.setHeaderText("Guardar configuración actual de pads");
            dialog.setContentText("Introduce un nombre único para este Kit:");

            java.util.Optional<String> result = dialog.showAndWait();

            result.ifPresent(kitName -> {
                com.dam.audiodigital_tfg.db.DrumKitDAO dao = new com.dam.audiodigital_tfg.db.DrumKitDAO();
                dao.saveCustomKit(kitName, drumNotes, drumNames);

                saveKitBtn.setText("✅ Guardado: " + kitName);
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> saveKitBtn.setText("💾 Guardar Kit..."));
                    }
                }, 2000);
            });
        });

        grid.add(saveKitBtn, 0, 4, 4, 1);

        return grid;
    }
    @Override
    public void stop() {

        System.out.println("Cerrando aplicación...");

        if (metroController != null) {
            metroController.stopEngine();
        }

        if (midiEngine != null) {
            midiEngine.close();
        }

        System.exit(0);
    }


    public static void main(String[] args) {
        launch(args);
    }
}