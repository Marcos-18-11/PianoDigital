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
import java.util.List;
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

        // =========================
        // 1. INICIALIZACIÓN Y METRÓNOMO
        // =========================
        com.dam.audiodigital_tfg.db.DatabaseManager.initDatabase();
        midiEngine = new MidiEngine();
        midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO);

        FXMLLoader fxmlLoader = new FXMLLoader(VirtualKeyboard.class.getResource("MetronomeView.fxml"));
        Parent metronomeRoot = fxmlLoader.load();
        metroController = fxmlLoader.getController();

        if (metroController != null) {
            metroController.setMidiEngine(midiEngine);
        }

        // =========================
        // 2. RAÍZ DE LA PESTAÑA DEL PIANO
        // =========================
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");

        // ====================
        // 3. PANEL IZQUIERDO
        // ====================
        VBox leftTransport = new VBox(15);
        leftTransport.setAlignment(Pos.TOP_CENTER);
        leftTransport.setPadding(new Insets(20));
        leftTransport.setPrefWidth(220);
        leftTransport.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);");

        Label lblControles = new Label("CONTROLES");
        lblControles.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Selector de Instrumento Principal
        ComboBox<String> instrumentSelector = new ComboBox<>();
        instrumentSelector.getItems().addAll(
                "Piano Acústico", "Piano Eléctrico", "Órgano", "Órgano de Iglesia",
                "Guitarra Acústica", "Guitarra Jazz", "Bajo Eléctrico", "Contrabajo",
                "Viola", "Orquesta", "Coro", "Trompeta", "Cuerno Francés",
                "Saxofón", "Flauta", "Campanas Tubulares", "Sitar",
                "Synth Lead", "Warm Pad", "Brightness"
        );
        instrumentSelector.setValue("Piano Acústico");
        instrumentSelector.setMaxWidth(Double.MAX_VALUE);
        instrumentSelector.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");

        instrumentSelector.setOnAction(event -> {
            String selected = instrumentSelector.getValue();
            switch (selected) {
                case "Piano Acústico": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO); break;
                case "Piano Eléctrico": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO_ELECTRIC); break;
                case "Órgano": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_ORGAN); break;
                case "Órgano de Iglesia": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_CHURCH_ORGAN); break;
                case "Guitarra Acústica": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_GUITAR); break;
                case "Guitarra Jazz": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_JAZZ_GUITAR); break;
                case "Bajo Eléctrico": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_ELECTRIC_BASS); break;
                case "Contrabajo": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_CONTRABASS); break;
                case "Viola": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_VIOLA); break;
                case "Orquesta": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_ORCHESTA); break;
                case "Coro": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_CHOIR); break;
                case "Trompeta": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_TRUMPET); break;
                case "Corno Francés": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_FRENCH_HORN); break;
                case "Saxofón": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_SAX); break;
                case "Flauta": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_FLUTE); break;
                case "Campanas Tubulares": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_TUBULAR_BELLS); break;
                case "Sitar": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_SITAR); break;
                case "Synth Lead": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_SINTH_LEAD); break;
                case "Warm Pad": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_WARM_PAD); break;
                case "Brightness": midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_BRIGHTNESS); break;
            }
        });

        Button recBtn = new Button("🔴 REC");
        recBtn.setMaxWidth(Double.MAX_VALUE);
        recBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button stopBtn = new Button("⏹ STOP");
        stopBtn.setMaxWidth(Double.MAX_VALUE);
        stopBtn.setDisable(true);
        stopBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand;");

        // Gestor de Grabaciones
        com.dam.audiodigital_tfg.db.SessionDAO gestorDAO = new com.dam.audiodigital_tfg.db.SessionDAO();
        Button btnGestorGrabaciones = new Button(" Mis Grabaciones");
        btnGestorGrabaciones.setMaxWidth(Double.MAX_VALUE);
        btnGestorGrabaciones.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        btnGestorGrabaciones.setOnAction(e -> {
            javafx.stage.Stage ventanaGestor = new javafx.stage.Stage();
            ventanaGestor.setTitle("Gestor de Grabaciones");

            javafx.scene.layout.VBox layoutGestor = new javafx.scene.layout.VBox(15);
            layoutGestor.setPadding(new javafx.geometry.Insets(20));
            layoutGestor.setStyle("-fx-background-color: #1e1e1e;");

            javafx.scene.control.ListView<com.dam.audiodigital_tfg.db.SessionDAO.SessionInfo> listaGrabaciones = new javafx.scene.control.ListView<>();
            listaGrabaciones.getItems().addAll(gestorDAO.getAllSessions());
            listaGrabaciones.setPrefHeight(250);

            Button btnBorrarRegistro = new Button(" Eliminar Seleccionada");
            btnBorrarRegistro.setMaxWidth(Double.MAX_VALUE);
            btnBorrarRegistro.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");

            btnBorrarRegistro.setOnAction(event -> {
                com.dam.audiodigital_tfg.db.SessionDAO.SessionInfo seleccionada = listaGrabaciones.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    gestorDAO.deleteSession(seleccionada.id);
                    listaGrabaciones.getItems().remove(seleccionada);
                }
            });

            layoutGestor.getChildren().addAll(new javafx.scene.control.Label("Tus grabaciones:"), listaGrabaciones, btnBorrarRegistro);
            ventanaGestor.setScene(new javafx.scene.Scene(layoutGestor, 400, 400));
            ventanaGestor.show();
        });

        // Modo Aprendizaje (Mapeo)
        ToggleButton btnMapeo = new ToggleButton(" MIDI/Key Learn");
        btnMapeo.setMaxWidth(Double.MAX_VALUE);
        btnMapeo.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Label infoBocadillo = new Label(" MODO APRENDIZAJE:\nPulsa una tecla en pantalla y luego en tu teclado físico.");
        infoBocadillo.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-padding: 10; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: bold;");
        infoBocadillo.setWrapText(true);
        infoBocadillo.setVisible(false);
        infoBocadillo.setManaged(false);

        btnMapeo.setOnAction(e -> {
            boolean activo = btnMapeo.isSelected();
            mappingManager.setLearnMode(activo);

            if (activo) {
                btnMapeo.setText("🛑 Escuchando...");
                btnMapeo.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
                infoBocadillo.setVisible(true);
                infoBocadillo.setManaged(true);
                stopBtn.setDisable(false);
            } else {
                btnMapeo.setText("🎧 MIDI/Key Learn");
                btnMapeo.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white; -fx-font-weight: bold;");
                infoBocadillo.setVisible(false);
                infoBocadillo.setManaged(false);
                if (recBtn.getText().equals("🔴 REC")) stopBtn.setDisable(true);
            }
        });

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
                    sessionDAO.saveSession(name, midiEngine.getCurrentInstrument(), midiEngine.getRecordedNotes());
                });
            }
        });

        leftTransport.getChildren().addAll(lblControles, new javafx.scene.control.Separator(), instrumentSelector, new javafx.scene.control.Separator(), recBtn, stopBtn, btnGestorGrabaciones, new javafx.scene.control.Separator(), btnMapeo, infoBocadillo);
        root.setLeft(leftTransport);

        // =================
        // 4. ZONA CENTRAL
        // =================
        VBox centerWorkspace = new VBox(40);
        centerWorkspace.setAlignment(Pos.TOP_CENTER);
        centerWorkspace.setPadding(new Insets(0, 20, 0, 20));

        // Rack Superior (Mezclador Horizontal + Efectos)

        HBox topRack = new HBox(30);
        topRack.setAlignment(Pos.CENTER);

        HBox mixerHBox = new HBox(15);
        mixerHBox.setAlignment(Pos.CENTER);
        mixerHBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        Map<Integer, Slider> vSliders = new HashMap<>();

        // Construcción de los 4 Canales
        for (int i = 0; i < 4; i++) {
            int channelIdx = i + 1; //  Aislamiento de canales (1 al 4)

            VBox channelRack = new VBox(8);
            channelRack.setAlignment(Pos.CENTER);
            channelRack.setPadding(new Insets(10));
            channelRack.setStyle("-fx-background-color: #3c3f41; -fx-background-radius: 8; -fx-border-color: #00ffcc; -fx-border-width: 1px;");
            channelRack.setPrefWidth(110);

            Label lblTrack = new Label("PISTA " + channelIdx);
            lblTrack.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold; -fx-font-size: 11px;");

            Slider volSlider = new Slider(0, 127, 100);
            volSlider.setOrientation(Orientation.VERTICAL);
            volSlider.setPrefHeight(100);
            int ccMap = (i == 0) ? 82 : (i == 1) ? 83 : (i == 2) ? 85 : 17;
            vSliders.put(ccMap, volSlider);

            volSlider.valueProperty().addListener((obs, old, val) -> {
                midiEngine.setChannelVolume(channelIdx, val.intValue());
            });

            ComboBox<com.dam.audiodigital_tfg.db.SessionDAO.SessionInfo> sessionSelector = new ComboBox<>();
            sessionSelector.setPromptText("Cargar...");
            sessionSelector.setPrefWidth(90);
            sessionSelector.setStyle("-fx-font-size: 10px;");

            final java.util.List<com.dam.audiodigital_tfg.RecordedNote>[] loadedNotes = new java.util.List[1];

            sessionSelector.setOnShowing(e -> {
                sessionSelector.getItems().setAll(new com.dam.audiodigital_tfg.db.SessionDAO().getAllSessions());
            });

            Button playTrackBtn = new Button("▶");
            playTrackBtn.setMaxWidth(Double.MAX_VALUE);
            playTrackBtn.setDisable(true);
            playTrackBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

            sessionSelector.setOnAction(e -> {
                var selected = sessionSelector.getValue();
                if (selected != null) {
                    loadedNotes[0] = new com.dam.audiodigital_tfg.db.SessionDAO().getNotesBySession(selected.id);
                    if (midiEngine.isChannelPlaying(channelIdx)) {
                        midiEngine.stopSessionPlayback(channelIdx);
                    }
                    playTrackBtn.setText("▶");
                    playTrackBtn.setDisable(false);
                    playTrackBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            });

            playTrackBtn.setOnAction(e -> {
                if (loadedNotes[0] != null && !loadedNotes[0].isEmpty()) {
                    if (!midiEngine.isChannelPlaying(channelIdx)) {
                        var selectedSession = sessionSelector.getValue();
                        midiEngine.changeChannelInstrument(channelIdx, selectedSession.instrumentId);
                        midiEngine.playSession(loadedNotes[0], channelIdx);
                        playTrackBtn.setText("⏹");
                        playTrackBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        midiEngine.stopSessionPlayback(channelIdx);
                        playTrackBtn.setText("▶");
                        playTrackBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                }
            });

            channelRack.getChildren().addAll(lblTrack, volSlider, sessionSelector, playTrackBtn);
            mixerHBox.getChildren().add(channelRack);
        }

        midiEngine.setControlChangeListener((cc, value) -> {
            if (vSliders.containsKey(cc)) {
                vSliders.get(cc).setValue(value);
            }
        });

        // Rack de Efectos
        VBox effectsRack = new VBox(15);
        effectsRack.setAlignment(Pos.CENTER);
        effectsRack.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        Label lblFX = new Label("EFECTOS (FX)");
        lblFX.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold; -fx-font-size: 14px;");

        VBox reverbBox = new VBox(5);
        reverbBox.setAlignment(Pos.CENTER);
        Label lblRev = new Label(" Reverb");
        lblRev.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Slider revSlider = new Slider(0, 127, 0);
        revSlider.setPrefWidth(100);
        revSlider.valueProperty().addListener((obs, old, val) -> midiEngine.setReverbLevel(val.intValue()));
        reverbBox.getChildren().addAll(lblRev, revSlider);

        VBox satBox = new VBox(5);
        satBox.setAlignment(Pos.CENTER);
        ToggleButton btnSaturation = new ToggleButton("🔥 Saturación");
        btnSaturation.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand;");
        Slider satSlider = new Slider(0, 10, 0);
        satSlider.setPrefWidth(100);

        satSlider.valueProperty().addListener((obs, old, val) -> {
            midiEngine.setSaturationDrive(val.floatValue());
            if (val.floatValue() > 0.1) {
                btnSaturation.setSelected(true);
                btnSaturation.setStyle("-fx-background-color: #ff5722; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                btnSaturation.setSelected(false);
                btnSaturation.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
            }
        });

        btnSaturation.setOnAction(event -> {
            boolean isOn = btnSaturation.isSelected();
            midiEngine.setSaturationEnabled(isOn);
            btnSaturation.setStyle(isOn ? "-fx-background-color: #ff5722; -fx-text-fill: white; -fx-font-weight: bold;" : "-fx-background-color: #555555; -fx-text-fill: white;");
        });
        satBox.getChildren().addAll(btnSaturation, satSlider);

        ToggleButton btnDelay = new ToggleButton(" Delay");
        btnDelay.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand;");
        btnDelay.setOnAction(event -> {
            boolean isOn = btnDelay.isSelected();
            midiEngine.setDelayEnabled(isOn);
            btnDelay.setStyle(isOn ? "-fx-background-color: #00E676; -fx-text-fill: black; -fx-font-weight: bold;" : "-fx-background-color: #555555; -fx-text-fill: white;");
        });

        effectsRack.getChildren().addAll(lblFX, new javafx.scene.control.Separator(), reverbBox, satBox, btnDelay);
        topRack.getChildren().addAll(mixerHBox, effectsRack);

        // Piano Virtual (Abajo)
        HBox keyboardContainer = new HBox(0); // Pegamos las teclas
        keyboardContainer.setAlignment(Pos.CENTER);

            // CARCASA AJUSTE
        keyboardContainer.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);

        //  REDUCIR EL PADDING
        keyboardContainer.setStyle(
                "-fx-background-color: #607D8B; " +
                        "-fx-padding: 35; " + // Ajuste
                        "-fx-background-radius: 14; " + // Esquinas redondeadas
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 14, 0, 0, 0);" // Sombra
        );

        createKeyboard(keyboardContainer, 2);

        centerWorkspace.getChildren().addAll(topRack, keyboardContainer);
        root.setCenter(centerWorkspace);
        // =========================
        // 5. PANEL DERECHO (Controles de Octava)
        // =========================
        VBox octaveControls = new VBox(10);
        octaveControls.setAlignment(Pos.CENTER);
        octaveControls.setPadding(new Insets(0, 0, 0, 20));

        Label octaveLabel = new Label("Octava: C4");
        octaveLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000;");

        Button upButton = new Button("Up");
        upButton.setPrefSize(50, 40);
        upButton.setOnAction(event -> {
            if (baseMidiNote <= 108) {
                baseMidiNote += 12;
                octaveLabel.setText("Octava: C" + (baseMidiNote / 12 - 1));
                createKeyboard(keyboardContainer, 2);
            }
        });

        Button downButton = new Button("Down");
        downButton.setPrefSize(50, 40);
        downButton.setOnAction(event -> {
            if (baseMidiNote >= 12) {
                baseMidiNote -= 12;
                octaveLabel.setText("Octava: C" + (baseMidiNote / 12 - 1));
                createKeyboard(keyboardContainer, 2);
            }
        });

        octaveControls.getChildren().addAll(upButton, octaveLabel, downButton);
        root.setRight(octaveControls);

        // =========================
        // 6. PESTAÑAS Y SPLITPANE
        // =========================
        javafx.scene.control.TabPane tabPane = new javafx.scene.control.TabPane();
        tabPane.setStyle("-fx-tab-min-width: 120px; -fx-tab-min-height: 35px; -fx-font-weight: bold; -fx-background-color: #1e1e1e;");

        javafx.scene.control.Tab tabPiano = new javafx.scene.control.Tab(" Teclado Virtual", root);
        tabPiano.setClosable(false);

        javafx.scene.control.Tab tabDrums = new javafx.scene.control.Tab(" Drum Pad", createDrumPad());
        tabDrums.setClosable(false);

        tabPane.getTabs().addAll(tabPiano, tabDrums);

        Label metronomeIndicator = new Label("      M E T R Ó N O M O     🔽");
        metronomeIndicator.setMaxWidth(Double.MAX_VALUE);
        metronomeIndicator.setAlignment(Pos.CENTER);
        metronomeIndicator.setStyle("-fx-background-color: #121212; -fx-text-fill: #00ffcc; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5;");

        VBox bottomArea = new VBox();
        bottomArea.getChildren().addAll(metronomeIndicator, tabPane);

        javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(metronomeRoot, bottomArea);
        splitPane.setDividerPositions(0.35f);

        // Dimensiones ampliadas para encajar el Mixer

        Scene scene = new Scene(splitPane, 1250, 800);

        // =========================
        // 7. LISTENERS GLOBALES (Mapeo y Teclado)
        // =========================
        midiEngine.setActionTriggerListener(new MidiEngine.ActionTriggerListener() {
            @Override
            public void onActionTriggered(String actionId) {
                switch (actionId) {
                    case "ACTION_REC": recBtn.fire(); break;
                    case "ACTION_STOP": stopBtn.fire(); break;
                }
            }

            @Override
            public void onMappingSuccess() {
                recBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-width: 0;");
                stopBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-border-width: 0;");
                System.out.println(" Mapeo capturado con éxito.");
            }
        });

        java.util.Set<String> notasSonando = new java.util.HashSet<>();

        scene.setOnKeyPressed(event -> {
            javafx.scene.input.KeyCode code = event.getCode();

            if (mappingManager.isLearnMode()) {
                if (mappingManager.getWaitingAction() != null) {
                    boolean mapeado = mappingManager.mapPcKey(code);
                    if (mapeado) {
                        recBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-width: 0;");
                        stopBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-border-width: 0;");
                        System.out.println("Tecla [" + code + "] vinculada con éxito.");
                    }
                }
            } else {
                String accion = mappingManager.getActionForPcKey(code);

                if (accion != null) {
                    if (accion.equals("ACTION_REC")) {
                        recBtn.fire();
                    } else if (accion.equals("ACTION_STOP")) {
                        stopBtn.fire();
                    }
                    else if (accion.startsWith("DRUM_")) {
                        try {
                            int note = Integer.parseInt(accion.split("_")[1]);
                            if (midiEngine != null) midiEngine.playPad(note, 127);
                        } catch (Exception ex) { }
                    }
                    else if (accion.startsWith("PIANO_")) {
                        if (notasSonando.contains(accion)) return;
                        notasSonando.add(accion);

                        try {
                            int note = Integer.parseInt(accion.split("_")[1]);
                            int notaTranspuesta = note + (baseMidiNote - 60);

                            if (midiEngine != null && notaTranspuesta >= 0 && notaTranspuesta <= 127) {
                                midiEngine.noteOn(notaTranspuesta, 127);
                            }
                        } catch (Exception ex) { }
                    }
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            if (!mappingManager.isLearnMode()) {
                String accion = mappingManager.getActionForPcKey(event.getCode());
                if (accion != null && accion.startsWith("PIANO_")) {
                    notasSonando.remove(accion);
                    try {
                        int note = Integer.parseInt(accion.split("_")[1]);
                        if (midiEngine != null) midiEngine.noteOff(note);
                    } catch (Exception ex) { }
                }
            }
        });

        primaryStage.setTitle("Estación Audio - Digital Audio Workstation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createKeyboard(HBox container, int numOctaves) {
        container.getChildren().clear();

        //  CSS PARA LAS TECLAS
        String whiteKeyNormal = "-fx-background-color: linear-gradient(to bottom, #ffffff 0%, #e6e6e6 100%); " +
                "-fx-border-color: #cccccc; -fx-border-width: 1 1 2 1; " +
                "-fx-background-radius: 0 0 5 5; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 1, 2);";

        String whiteKeyPressed = "-fx-background-color: linear-gradient(to bottom, #e6e6e6 0%, #cccccc 100%); " +
                "-fx-border-color: #999999; -fx-border-width: 1; " +
                "-fx-background-radius: 0 0 5 5; -fx-cursor: hand;";

        String blackKeyNormal = "-fx-background-color: linear-gradient(to right, #333333 0%, #111111 50%, #333333 100%); " +
                "-fx-text-fill: white; -fx-background-radius: 0 0 4 4; " +
                "-fx-border-color: #000000; -fx-border-width: 1; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 2, 3);";

        String blackKeyPressed = "-fx-background-color: linear-gradient(to right, #1a1a1a 0%, #000000 50%, #1a1a1a 100%); " +
                "-fx-text-fill: #00ffcc; -fx-background-radius: 0 0 4 4; -fx-cursor: hand;";

        for (int octave = 0; octave < numOctaves; octave++) {
            for (int noteInOctave = 0; noteInOctave < 12; noteInOctave++) {
                String noteName = OCTAVE_STRUCTURE.get(noteInOctave);
                boolean isBlack = noteName.contains("#");

                Button key = new Button(noteName);
                key.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 10));

                if (isBlack) {
                    key.setStyle(blackKeyNormal);
                    key.setPrefSize(35, 120);
                    key.setAlignment(Pos.BOTTOM_CENTER);
                    key.setPadding(new Insets(0, 0, 15, 0)); // Texto abajo

                    // VISUAL: Margen negativo para que monte sobre las blancas
                    HBox.setMargin(key, new Insets(0, -17, 60, -17));
                    // Fuerza a que se renderice por encima en JavaFX
                    key.setViewOrder(-1.0);
                } else {
                    key.setStyle(whiteKeyNormal);
                    key.setPrefSize(50, 180);
                    key.setAlignment(Pos.BOTTOM_CENTER);
                    key.setPadding(new Insets(0, 0, 10, 0)); // Texto pegado abajo
                    key.setViewOrder(0.0);
                }

                // Cálculo de IDs
                int actualMidiNote = baseMidiNote + (octave * 12) + noteInOctave;
                int notaMapeoFija = 60 + (octave * 12) + noteInOctave;
                final String actionId = "PIANO_" + notaMapeoFija;

                // Eventos de clic rediseñados para hundir la tecla
                key.setOnMousePressed(mouseEvent -> {
                    if (mappingManager.isLearnMode()) {
                        mappingManager.setWaitingAction(actionId);
                        key.setStyle("-fx-background-color: yellow; -fx-border-color: orange; -fx-border-width: 3px; -fx-cursor: hand;");
                    } else {
                        key.setStyle(isBlack ? blackKeyPressed : whiteKeyPressed);
                        midiEngine.noteOn(actualMidiNote, 100);
                    }
                });

                key.setOnMouseReleased(mouseEvent -> {
                    if (!mappingManager.isLearnMode() || !actionId.equals(mappingManager.getWaitingAction())) {
                        key.setStyle(isBlack ? blackKeyNormal : whiteKeyNormal);
                    }
                    midiEngine.noteOff(actualMidiNote);
                });

                key.setOnMouseExited(mouseEvent -> {
                    if (!mappingManager.isLearnMode() || !actionId.equals(mappingManager.getWaitingAction())) {
                        key.setStyle(isBlack ? blackKeyNormal : whiteKeyNormal);
                    }
                    midiEngine.noteOff(actualMidiNote);
                });

                container.getChildren().add(key);
            }
        }
    }

    private javafx.scene.layout.StackPane createDrumPad() {
        //  (Contenedor Exterior)
        javafx.scene.layout.StackPane outerContainer = new javafx.scene.layout.StackPane();
        outerContainer.setStyle("-fx-background-color: #ffffff;");

        // Contenedor vertical
        javafx.scene.layout.VBox centerStack = new javafx.scene.layout.VBox(15);
        centerStack.setAlignment(Pos.CENTER);
        centerStack.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);

        //  CARCASA DEL DRUM PAD
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(30));
        grid.setStyle(
                "-fx-background-color: #607D8B; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 0);"
        );

        // Mapeo estándar MIDI de Percusión
        int[] drumNotes = {
                49, 51, 52, 53,
                43, 45, 47, 48,
                42, 44, 46, 55,
                36, 38, 37, 39
        };

        String[] drumNames = {
                "Crash", "Ride", "China", "Bell",
                "Tom H", "Tom M", "Tom L", "Tom XL",
                "HiHat C", "Pedal", "HiHat O", "Splash",
                "KICK", "SNARE", "Rim", "Clap"
        };

        String padNormal =
                "-fx-background-color: linear-gradient(to bottom right, #2a2a2a 0%, #000000 100%); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 1, 2);";

        String padPressed =
                "-fx-background-color: linear-gradient(to bottom right, #66BB6A 0%, #43A047 100%); " +
                        "-fx-text-fill: black; -fx-background-radius: 10;";

        int count = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                Button pad = new Button(drumNames[count]);
                pad.setPrefSize(100, 100);
                pad.setStyle(padNormal);
                pad.setWrapText(true);
                pad.setAlignment(Pos.CENTER);

                final int midiNote = drumNotes[count];
                final String actionId = "DRUM_" + midiNote;

                pad.setOnMousePressed(e -> {
                    if (mappingManager.isLearnMode()) {
                        mappingManager.setWaitingAction(actionId);
                        pad.setStyle(padNormal + "-fx-border-color: #FFEA00; -fx-border-width: 3px; -fx-border-radius: 10;");
                    } else {
                        pad.setStyle(padPressed);
                        if (midiEngine != null) {
                            midiEngine.playPad(midiNote, 127);
                        }
                    }
                });

                pad.setOnMouseReleased(e -> {
                    if (!mappingManager.isLearnMode() || !actionId.equals(mappingManager.getWaitingAction())) {
                        pad.setStyle(padNormal);
                    }
                });

                grid.add(pad, col, row);
                count++;
            }
        }

        // 🚨 3. EL BOTÓN DE GUARDAR (Fuera, más estrecho y Azul Oscuro)
        Button saveKitBtn = new Button("💾 Guardar Kit...");
        saveKitBtn.setPrefWidth(160); // Ancho fijo y estilizado (más estrecho)
        // Fondo Azul Oscuro (#1A237E), texto blanco y bordes redondeados suaves
        saveKitBtn.setStyle(
                "-fx-background-color: #1A237E; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 6; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);"
        );

        saveKitBtn.setOnAction(e -> {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("Nuevo Kit MiniLab");
            dialog.setTitle("Guardar Drum Kit");
            dialog.setHeaderText("Guardar configuración actual de pads");
            dialog.setContentText("Introduce un nombre único para este Kit:");

            java.util.Optional<String> result = dialog.showAndWait();

            result.ifPresent(kitName -> {
                com.dam.audiodigital_tfg.db.DrumKitDAO dao = new com.dam.audiodigital_tfg.db.DrumKitDAO();
                dao.saveCustomKit(kitName, drumNotes, drumNames);

                saveKitBtn.setText(" Guardado");
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> saveKitBtn.setText(" Guardar Kit..."));
                    }
                }, 2000);
            });
        });

        // Metemos la carcasa y luego el botón abajo en la pila vertical
        centerStack.getChildren().addAll(grid, saveKitBtn);

        //  lienzo blanco
        outerContainer.getChildren().add(centerStack);
        return outerContainer;
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