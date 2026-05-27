package com.dam.audiodigital_tfg;

import com.dam.audiodigital_tfg.audio.MetronomeEngine;
import com.dam.audiodigital_tfg.audio.MidiEngine;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class MetronomeController {

    @FXML private TextField bpmTextField;
    @FXML private Slider bpmSlider;
    @FXML private Pane visualPane;

    // Caja A
    @FXML private Button playBtnA;
    @FXML private ComboBox<Integer> beatsComboA;
    @FXML private ComboBox<String> instComboA;
    @FXML private Slider volSliderA;
    private boolean isPlayingA = false;
    private List<Circle> dotsA = new ArrayList<>();
    private Circle indicatorA;

    // Caja B
    @FXML private Button playBtnB;
    @FXML private ComboBox<Integer> beatsComboB;
    @FXML private ComboBox<String> instComboB;
    @FXML private Slider volSliderB;
    private boolean isPlayingB = false;
    private List<Circle> dotsB = new ArrayList<>();
    private Circle indicatorB;

    private MetronomeEngine engine;

    // Dimensiones y Radios
    private final double centerX = 170.0;
    private final double centerY = 170.0;
    private final double radiusA = 135.0; // Órbita exterior
    private final double radiusB = 85.0;  // Órbita interior

    // Motores de Animación Continua
    private Rotate rotateA = new Rotate(0, centerX, centerY);
    private Rotate rotateB = new Rotate(0, centerX, centerY);
    private Timeline timelineA;
    private Timeline timelineB;

    public void setMidiEngine(MidiEngine midiEngine) {
        this.engine = new MetronomeEngine(midiEngine.getPercussionChannel());
        setupVisualCallbacks();
    }

    private void setupVisualCallbacks() {
        engine.setOnTickA(beatIndex -> Platform.runLater(() -> {
            flashDot(dotsA, beatIndex, "#7289da");

            // TRUCO DE SINCRONIZACIÓN: En el primer golpe, forzamos a la animación a reiniciar desde 0

            if (beatIndex == 0 && timelineA != null) timelineA.playFromStart();
        }));

        engine.setOnTickB(beatIndex -> Platform.runLater(() -> {
            flashDot(dotsB, beatIndex, "#f04747");
            if (beatIndex == 0 && timelineB != null) timelineB.playFromStart();
        }));
    }

    // =====================
    // DIBUJO GEOMÉTRICO
    // =====================
    private void drawRings() {
        if (visualPane == null) return;

        visualPane.getChildren().clear();
        dotsA.clear();
        dotsB.clear();

        int bA = beatsComboA.getValue() != null ? beatsComboA.getValue() : 4;
        int bB = beatsComboB.getValue() != null ? beatsComboB.getValue() : 3;

        // Círculos de fondo ---
        drawBackgroundTrack(centerX, centerY, radiusA); // Órbita A (Gris sutil)
        drawBackgroundTrack(centerX, centerY, radiusB); // Órbita B (Gris sutil)

        // Dibujamos polígonos y puntos ENCIMA (Z-ordering automático por orden de adición)
        drawRingWithPolygon(bA, radiusA, dotsA, "#7289da"); // Ritmo A Azul
        drawRingWithPolygon(bB, radiusB, dotsB, "#f04747"); // Ritmo B Rojo

        // Preparar indicadores de rotación y colocarlos encima
        Platform.runLater(this::initializeIndicators);
    }

    // helper para dibujar círculos de fondo
    private void drawBackgroundTrack(double cx, double cy, double radius) {
        Circle track = new Circle(cx, cy, radius);
        track.setFill(Color.TRANSPARENT);
        track.setStroke(Color.web("#555555"));    // Gris oscuro
        track.setStrokeWidth(1.5);                  // Línea fina
        track.setOpacity(0.4);                      // Opacidad baja
        visualPane.getChildren().add(track);        // Añadido primero = al fondo
    }

    private void drawRingWithPolygon(int numBeats, double radius, List<Circle> dotList, String colorHex) {
        double angleStep = (2 * Math.PI) / Math.max(1, numBeats);
        double startAngle = -Math.PI / 2;
        List<Double> polygonPoints = new ArrayList<>();

        // 1. Calcular puntos equidistantes y dibujar PUNTOS
        for (int i = 0; i < numBeats; i++) {
            double angle = startAngle + (i * angleStep);

            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            polygonPoints.add(x);
            polygonPoints.add(y);

            Circle dot = new Circle(x, y, 10.0); // Puntos más grandes (radio 10)
            dot.setFill(Color.web("#3e4249")); // Color base oscuro
            dot.setStroke(Color.web("#1a1c1e")); // Borde casi negro
            dot.setStrokeWidth(2.5); // Borde grueso
            dotList.add(dot);
        }

        // 2. DIBUJAR POLÍGONO UNIENDO PUNTOS
        Polygon shape = new Polygon();
        shape.getPoints().addAll(polygonPoints);
        shape.setFill(Color.TRANSPARENT); // Hueco por dentro
        shape.setStroke(Color.web(colorHex)); // Borde de color (Azul o Rojo)
        shape.setStrokeWidth(2.5); // Grosor de línea
        shape.setStrokeType(StrokeType.CENTERED); // Centrar la línea en los puntos

        // Añadimos el polígono primero (detrás de los puntos)
        visualPane.getChildren().add(shape);
        // Añadimos los puntitos individuales encima
        visualPane.getChildren().addAll(dotList);
    }

    // =========================================================
    // ANIMACIÓN CONTINUA / SECUENCIADOR (Punto 2)
    // =========================================================
    private void initializeIndicators() {
        if (indicatorA == null) {
            indicatorA = createTargetingRing("#a6dcef", radiusA); // Azul más claro para el anillo selector
            indicatorA.getTransforms().add(rotateA);
        }
        if (indicatorB == null) {
            indicatorB = createTargetingRing("#ffcccb", radiusB); // Rojo más claro para el anillo selector
            indicatorB.getTransforms().add(rotateB);
        }

        // Z-Ordering: Asegurar que los selectores estén SIEMPRE encima de todo
        if (!visualPane.getChildren().contains(indicatorA)) visualPane.getChildren().add(indicatorA);
        if (!visualPane.getChildren().contains(indicatorB)) visualPane.getChildren().add(indicatorB);
        indicatorA.toFront();
        indicatorB.toFront();
    }

    // Crea un anillo selector hueco que se superpone perfectamente sobre el punto
    private Circle createTargetingRing(String colorHex, double orbitRadius) {
        Circle ring = new Circle(14.0); // Más grande que el punto normal (radio 10.0) para que pase por fuera
        ring.setFill(Color.TRANSPARENT); // Hueco por dentro
        ring.setStroke(Color.web(colorHex)); // Color más claro
        ring.setStrokeWidth(3.5); // Grosor de borde alto
        ring.setVisible(false); // Inicialmente invisible
        // Lo posicionamos arriba del todo (Ángulo -PI/2)
        ring.setCenterX(centerX);
        ring.setCenterY(centerY - orbitRadius);
        return ring;
    }

    private void updateTimelines(double bpm) {
        // Matemática: Cuánto dura una vuelta entera en milisegundos
        // Ciclo maestro de 4 tiempos = (60s / BPM) * 4
        double cycleMs = (60000.0 / bpm) * 4;

        if (timelineA != null) timelineA.stop();
        timelineA = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotateA.angleProperty(), 0)),
                new KeyFrame(Duration.millis(cycleMs), new KeyValue(rotateA.angleProperty(), 360, Interpolator.LINEAR))
        );
        timelineA.setCycleCount(Animation.INDEFINITE);
        if (isPlayingA) timelineA.play();

        if (timelineB != null) timelineB.stop();
        timelineB = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotateB.angleProperty(), 0)),
                new KeyFrame(Duration.millis(cycleMs), new KeyValue(rotateB.angleProperty(), 360, Interpolator.LINEAR))
        );
        timelineB.setCycleCount(Animation.INDEFINITE);
        if (isPlayingB) timelineB.play();
    }

    private void flashDot(List<Circle> dotList, int beatIndex, String activeColor) {
        if (dotList.isEmpty() || beatIndex >= dotList.size()) return;

        Circle dot = dotList.get(beatIndex);
        dot.setFill(Color.web(activeColor)); // Encender de color del ritmo

        PauseTransition pause = new PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> dot.setFill(Color.web("#3e4249"))); // Apagar volviendo a base oscura
        pause.play();
    }

    // ============================
    // INICIALIZACIÓN Y EVENTOS
    // ============================
    @FXML
    public void initialize() {
        // --- 1. LÓGICA DE BPM  ---
        bpmSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int currentBpm = newVal.intValue();
            bpmTextField.setText(String.valueOf(currentBpm));
            if (engine != null) engine.setBpm(currentBpm);
            updateTimelines(currentBpm); // Actualizar velocidad de animación
        });

        bpmTextField.setOnAction(event -> {
            try {
                int newBpm = Integer.parseInt(bpmTextField.getText());
                if (newBpm >= 40 && newBpm <= 240) {
                    bpmSlider.setValue(newBpm);
                } else {
                    bpmTextField.setText(String.valueOf((int) bpmSlider.getValue()));
                }
            } catch (NumberFormatException e) {
                bpmTextField.setText(String.valueOf((int) bpmSlider.getValue()));
            }
        });

        // --- 2. POBLAR DESPLEGABLES ---
        Integer[] beatOptions = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        beatsComboA.getItems().addAll(beatOptions);
        beatsComboB.getItems().addAll(beatOptions);

        instComboA.getItems().addAll(MetronomeEngine.INSTRUMENTS.keySet());
        instComboB.getItems().addAll(MetronomeEngine.INSTRUMENTS.keySet());

        // Valores por defecto
        beatsComboA.setValue(4);
        instComboA.setValue("Woodblock Alto");
        beatsComboB.setValue(3);
        instComboB.setValue("Cencerro");

        // --- 3. LISTENERS CAJA A ---
        beatsComboA.setOnAction(e -> {
            if (engine != null) { engine.stop(); engine.setBeatsA(beatsComboA.getValue()); }
            drawRings();
            updateTimelines(bpmSlider.getValue()); // Importante: reiniciar timelines con BPM actual
        });
        instComboA.setOnAction(e -> { if (engine != null) engine.setInstrumentA(instComboA.getValue()); });
        volSliderA.valueProperty().addListener((obs, oldV, newV) -> { if (engine != null) engine.setVolumeA(newV.intValue()); });

        // --- 4. LISTENERS CAJA B ---
        beatsComboB.setOnAction(e -> {
            if (engine != null) { engine.stop(); engine.setBeatsB(beatsComboB.getValue()); }
            drawRings();
            updateTimelines(bpmSlider.getValue()); // Importante: reiniciar timelines con BPM actual
        });
        instComboB.setOnAction(e -> { if (engine != null) engine.setInstrumentB(instComboB.getValue()); });
        volSliderB.valueProperty().addListener((obs, oldV, newV) -> { if (engine != null) engine.setVolumeB(newV.intValue()); });

        // Dibujamos los anillos por primera vez e inicializamos timelines
        Platform.runLater(() -> {
            drawRings();
            updateTimelines(120); // Inicializar velocidad al arrancar
        });
    }

    @FXML
    private void toggleA() {
        isPlayingA = !isPlayingA;
        if (engine != null) engine.setMutedA(!isPlayingA);
        updateButtonUI(playBtnA, isPlayingA, "#7289da");

        // Control de visibilidad del selector continuo
        if (indicatorA != null) {
            indicatorA.setVisible(isPlayingA);
        }
        // Arrancar o detener timeline de JavaFX
        if (isPlayingA && timelineA != null) timelineA.play();
        else if (timelineA != null) timelineA.stop();
    }

    @FXML
    private void toggleB() {
        isPlayingB = !isPlayingB;
        if (engine != null) engine.setMutedB(!isPlayingB);
        updateButtonUI(playBtnB, isPlayingB, "#f04747");

        // Control de visibilidad del selector continuo
        if (indicatorB != null) {
            indicatorB.setVisible(isPlayingB);
        }
        // Arrancar o detener timeline de JavaFX
        if (isPlayingB && timelineB != null) timelineB.play();
        else if (timelineB != null) timelineB.stop();
    }

    private void updateButtonUI(Button btn, boolean isPlaying, String activeColor) {
        if (isPlaying) {
            btn.setText("⏸ STOP");
            btn.setStyle("-fx-background-color: " + activeColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            btn.setText("▶ PLAY");
            btn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        }
    }

    public void stopEngine() {
        if (engine != null) engine.stop();
    }
}