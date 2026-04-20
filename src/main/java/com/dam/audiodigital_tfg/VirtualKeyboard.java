package com.dam.audiodigital_tfg;

import com.dam.audiodigital_tfg.audio.MidiEngine;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;


public class VirtualKeyboard extends Application {

    private MidiEngine midiEngine;

    private int baseMidiNote=60;
    private final String WHITE_KEY_STYLE="-fx-background-color: white; -fx-border-color: black; -fx-cursor: hand;";
    private final String BLACK_KEY_STYLE = "-fx-background-color: black; -fx-text-fill: white; -fx-cursor: hand;";

    private static final Map<Integer, String> OCTAVE_ESTRUCTURE=new HashMap<>();

    static {

        OCTAVE_ESTRUCTURE.put(0,"Do");
        OCTAVE_ESTRUCTURE.put(1,"Do#");
        OCTAVE_ESTRUCTURE.put(2,"Re");
        OCTAVE_ESTRUCTURE.put(3,"Re#");
        OCTAVE_ESTRUCTURE.put(4,"Mi");
        OCTAVE_ESTRUCTURE.put(5,"Fa");
        OCTAVE_ESTRUCTURE.put(6,"Fa#");
        OCTAVE_ESTRUCTURE.put(7,"Sol");
        OCTAVE_ESTRUCTURE.put(8,"Sol#");
        OCTAVE_ESTRUCTURE.put(9,"La");
        OCTAVE_ESTRUCTURE.put(10,"La#");
        OCTAVE_ESTRUCTURE.put(11,"Si");


    }

    @Override
    public void init() {
        //
        midiEngine = new MidiEngine();
        midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        VBox topPane = new VBox(10);
        topPane.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Virtual Keyboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ComboBox<String> instrumentSelector= new ComboBox<>();
        instrumentSelector.getItems().addAll("Piano", "Guitarra");

        instrumentSelector.setValue("Piano");
        instrumentSelector.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");

        instrumentSelector.setOnAction(event->{
            String selected=instrumentSelector.getValue();
            if("Piano".equals(selected)){
                midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO);
            } else if ("Guitarra".equals(selected)) {
                midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_GUITAR);
            }
        });
        topPane.getChildren().addAll(titleLabel,instrumentSelector);
        root.setTop(topPane);

        HBox keyboardContainer=new HBox(1);
        keyboardContainer.setAlignment(Pos.CENTER);
        keyboardContainer.setPadding(new Insets(20, 0,20,0));

        createKeyboard(keyboardContainer,2);
        root.setCenter(keyboardContainer);

        VBox octaveControls=new VBox(10);

        octaveControls.setAlignment(Pos.CENTER);
        octaveControls.setPadding(new Insets(0,0,0,20));

        Label octaveLabel=new Label("Octava: C4");
        octaveLabel.setStyle("-fx-font-weight: bold;");

        Button upButton=new Button("Up");
        upButton.setPrefSize(40, 40);
        upButton.setOnAction(actionEvent -> {
            if ((baseMidiNote<=96)){
                baseMidiNote+=12;
                octaveLabel.setText("Octava C" + (baseMidiNote/12-1));
            }
        });

        Button downButton = new Button("Down");
        downButton.setPrefSize(40, 40);
        downButton.setOnAction(event -> {

            if (baseMidiNote - 12 >= 0) {
                baseMidiNote -= 12;
                octaveLabel.setText("Octava: C" + (baseMidiNote/12 - 1));
            }
        });

        octaveControls.getChildren().addAll(upButton, octaveLabel, downButton);
        root.setRight(octaveControls);

       // Scene scene=new Scene(root, 1000, 500);

        primaryStage.setTitle("Music Box");
      // primaryStage.setScene(scene);
        primaryStage.show();

        javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(VirtualKeyboard.class.getResource("MetronomeView.fxml"));
        javafx.scene.Parent metronomeRoot = fxmlLoader.load();

        // 2. Creamos el SplitPane Vertical
        javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // 3. Añadimos el Metrónomo (arriba) y el Teclado (abajo)
        splitPane.getItems().addAll(metronomeRoot, root);

        // 4. Ajustamos la posición inicial de la barra separadora (30% arriba, 70% abajo)
        splitPane.setDividerPositions(0.3f);

        // 5. Metemos el SplitPane en la Escena (he subido un poco la altura total a 700)
        Scene scene = new Scene(splitPane, 1000, 700);

        primaryStage.setTitle("Music Box DAW");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createKeyboard(HBox container, int numOctaves){
        container.getChildren().clear();

        for(int octave=0;octave < numOctaves; octave++){
            for(int noteInOctave=0;noteInOctave<12;noteInOctave++){
                String noteName= OCTAVE_ESTRUCTURE.get(noteInOctave);
                boolean isBlack=noteName.contains("#");

                Button key = new Button(noteName);

                if(isBlack){
                    key.setStyle(BLACK_KEY_STYLE);
                    key.setPrefSize(35,125);
                } else{
                    key.setStyle(WHITE_KEY_STYLE);
                    key.setPrefSize(50, 200);
                }

                int finalMidiNote=(octave*12) + noteInOctave;

                key.setOnMousePressed(mouseEvent -> {
                    midiEngine.playNote(baseMidiNote +finalMidiNote,100,500);
                });
                container.getChildren().add(key);
            }
        }
    }

    @Override
    public void stop() {
        if (midiEngine != null) {
            midiEngine.close();

        }
    }

    public static void main(String[] args) {
        launch(args);
    }


}
