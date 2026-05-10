package com.dam.audiodigital_tfg;

import com.dam.audiodigital_tfg.audio.MetronomeEngine;
import com.dam.audiodigital_tfg.audio.MidiEngine;
import com.dam.audiodigital_tfg.db.SessionDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
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
        instrumentSelector.getItems().addAll("Piano", "Guitarra");
        instrumentSelector.setValue("Piano");
        instrumentSelector.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");

        instrumentSelector.setOnAction(event -> {
            String selected = instrumentSelector.getValue();
            if ("Piano".equals(selected)) {
                midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO);
            } else if ("Guitarra".equals(selected)) {
                midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_GUITAR);
            }
        });

        // 2. Botón de Importar
        Button importBtn = new Button("📂 Importar SoundFont (.sf2)");
        importBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        importBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Importar Banco de Sonidos (SoundFont)");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Bancos de Sonido", "*.sf2", "*.dls", "*.rmf")
            );

            java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                midiEngine.loadCustomSoundbank(selectedFile);
                titleLabel.setText("Cargado: " + selectedFile.getName());
            }
        });

        // Metemos el selector y el botón en la caja horizontal
        controlsHBox.getChildren().addAll(instrumentSelector, importBtn);

        // Metemos el título y la caja de controles en el panel superior
        topPane.getChildren().addAll(titleLabel, controlsHBox);
        root.setTop(topPane);


        // =========================
        // LEFT PANEL (Mezclador 4 Pistas)
        // =========================
        VBox leftPane = new VBox(15);
        leftPane.setAlignment(Pos.TOP_CENTER);
        leftPane.setPadding(new Insets(20));
        leftPane.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;"); // Línea divisoria a la derecha

        Label mixerLabel = new Label("Mezclador");
        mixerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        leftPane.getChildren().add(mixerLabel);

        // Creamos 4 botones con un bucle
        for (int i = 1; i <= 4; i++) {
            final int trackNumber = i; // Necesitamos que sea final para usarlo dentro del evento

            Button loadTrackBtn = new Button("📁 Pista " + trackNumber);
            loadTrackBtn.setPrefWidth(100);
            loadTrackBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

            loadTrackBtn.setOnAction(e -> {
                com.dam.audiodigital_tfg.db.SessionDAO dao = new com.dam.audiodigital_tfg.db.SessionDAO();
                java.util.List<com.dam.audiodigital_tfg.db.SessionDAO.SessionInfo> sessions = dao.getAllSessions();

                if (sessions.isEmpty()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Sin sesiones");
                    alert.setHeaderText(null);
                    alert.setContentText("Aún no tienes sesiones guardadas. ¡Graba algo primero!");
                    alert.showAndWait();
                    return;
                }

                // Creamos una ventana de diálogo con un desplegable (ComboBox)
                javafx.scene.control.ChoiceDialog<com.dam.audiodigital_tfg.db.SessionDAO.SessionInfo> dialog =
                        new javafx.scene.control.ChoiceDialog<>(sessions.get(0), sessions);
                dialog.setTitle("Cargar en Pista " + trackNumber);
                dialog.setHeaderText("Canal " + trackNumber + " - Volumen mapeado al fader");
                dialog.setContentText("Selecciona la sesión:");

                // Si el usuario elige una y le da a Aceptar...
                dialog.showAndWait().ifPresent(selectedSession -> {
                    java.util.List<com.dam.audiodigital_tfg.RecordedNote> savedNotes = dao.getNotesBySession(selectedSession.id);
                    if (!savedNotes.isEmpty()) {
                        System.out.println("Cargando: " + selectedSession.name + " en Canal " + (trackNumber - 1));

                        // Cambiamos el texto del botón para saber qué está cargado
                        loadTrackBtn.setText("▶ " + selectedSession.name);

                        // Reproducimos
                        midiEngine.playSession(savedNotes, trackNumber - 1);
                    }
                });
            });

            leftPane.getChildren().add(loadTrackBtn);
        }

        // Anclamos este panel a la izquierda de la pantalla
        root.setLeft(leftPane);


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
        // BARRA DE TRANSPORTE (REC / STOP)
        // =========================
        HBox transportBar = new HBox(10);
        transportBar.setAlignment(Pos.CENTER);

        Button recBtn = new Button("🔴 REC");
        Button stopBtn = new Button("⏹ STOP");
        stopBtn.setDisable(true); // Deshabilitado hasta que grabemos

        recBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
        stopBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");

        recBtn.setOnAction(e -> {
            midiEngine.startRecording();
            recBtn.setDisable(true);
            stopBtn.setDisable(false);
            recBtn.setText("⏺ GRABANDO...");
        });

        stopBtn.setOnAction(e -> {
            midiEngine.stopRecording();
            recBtn.setDisable(false);
            stopBtn.setDisable(true);
            recBtn.setText("🔴 REC");

            // Al parar, preguntamos nombre para guardar la sesión
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("Mi Sesión " + System.currentTimeMillis());
            dialog.setTitle("Guardar Grabación");
            dialog.setHeaderText("¡Grabación finalizada!");
            dialog.setContentText("Introduce el nombre de la sesión:");

            dialog.showAndWait().ifPresent(name -> {
                SessionDAO sessionDAO = new SessionDAO();
                sessionDAO.saveSession(name, midiEngine.getRecordedNotes());
            });
        });

        transportBar.getChildren().addAll(recBtn, stopBtn);

        // Añadimos la barra de transporte al topPane (debajo de los otros controles)
        topPane.getChildren().add(transportBar);
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

                // ... (código de creación del botón key) ...

                int finalMidiNote = (octave * 12) + noteInOctave;

                // Al hacer clic: tocamos la nota y cambiamos el color del botón (feedback visual)
                key.setOnMousePressed(mouseEvent -> {
                    key.setStyle("-fx-background-color: #a0a0a0; -fx-cursor: hand;"); // Gris al pulsar
                    midiEngine.noteOn(baseMidiNote + finalMidiNote, 100);
                });

                // Al soltar el clic: apagamos la nota y devolvemos el color original
                key.setOnMouseReleased(mouseEvent -> {
                    key.setStyle(isBlack ? BLACK_KEY_STYLE : WHITE_KEY_STYLE);
                    midiEngine.noteOff(baseMidiNote + finalMidiNote);
                });

                // Por si el usuario arrastra el ratón fuera del botón sin soltar el clic
                key.setOnMouseExited(mouseEvent -> {
                    key.setStyle(isBlack ? BLACK_KEY_STYLE : WHITE_KEY_STYLE);
                    midiEngine.noteOff(baseMidiNote + finalMidiNote);
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
        // 36: Kick (Bombo), 38: Snare (Caja), 42: Hi-Hat Cerrado, 49: Crash...
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

                // Evento al pulsar (Usamos el canal de percusión que nos presta el MidiEngine)
                pad.setOnMousePressed(e -> {
                    pad.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
                    if (midiEngine != null && midiEngine.getPercussionChannel() != null) {
                        midiEngine.getPercussionChannel().noteOn(midiNote, 127);
                    }
                });

                // Evento al soltar el clic (vuelve a su color original)
                pad.setOnMouseReleased(e -> {
                    pad.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
                });

                grid.add(pad, col, row);
                count++;
            }
        }

        // Botón para guardar el Kit actual en la Base de Datos
        Button saveKitBtn = new Button("💾 Guardar Kit...");
        saveKitBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        saveKitBtn.setOnAction(e -> {
            // 1. Creamos una ventana de diálogo para pedir el nombre
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("Nuevo Kit MiniLab");
            dialog.setTitle("Guardar Drum Kit");
            dialog.setHeaderText("Guardar configuración actual de pads");
            dialog.setContentText("Introduce un nombre único para este Kit:");

            // 2. Mostramos la ventana y capturamos la respuesta
            java.util.Optional<String> result = dialog.showAndWait();

            // 3. Si el usuario le da a "Aceptar" y ha escrito algo...
            result.ifPresent(kitName -> {
                com.dam.audiodigital_tfg.db.DrumKitDAO dao = new com.dam.audiodigital_tfg.db.DrumKitDAO();

                // Intentamos guardarlo. Si el nombre ya existe, el DAO saltará por el catch
                dao.saveCustomKit(kitName, drumNotes, drumNames);

                // Feedback visual rápido
                saveKitBtn.setText("✅ Guardado: " + kitName);

                // Volvemos a poner el texto original después de 2 segundos
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> saveKitBtn.setText("💾 Guardar Kit..."));
                    }
                }, 2000);
            });
        });

        // Añadimos el botón debajo de la cuadrícula (columna 0, fila 4, ocupando 4 columnas de ancho)
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