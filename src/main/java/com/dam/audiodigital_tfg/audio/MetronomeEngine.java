package com.dam.audiodigital_tfg.audio;

import javax.sound.midi.MidiChannel;
import java.util.HashMap;
import java.util.Map;

public class MetronomeEngine {
    private boolean isRunning = false;
    private int bpm = 120;

    // Parámetros Caja A
    private int beatsA = 4;
    private int instrumentA = 76;
    private int volumeA = 100;
    private boolean isMutedA = true; // Empieza silenciado

    // Parámetros Caja B
    private int beatsB = 3;
    private int instrumentB = 56;
    private int volumeB = 100;
    private boolean isMutedB = true; // Empieza silenciado

    private MidiChannel percussionChannel;
    private Thread threadA;
    private Thread threadB;

    // Callbacks para la Interfaz Gráfica

    private java.util.function.Consumer<Integer> onTickA;
    private java.util.function.Consumer<Integer> onTickB;



    public static final Map<String, Integer> INSTRUMENTS = new HashMap<>();
    static {
        INSTRUMENTS.put("Woodblock Alto", 76);
        INSTRUMENTS.put("Woodblock Bajo", 77);
        INSTRUMENTS.put("Click (Aro)", 37);
        INSTRUMENTS.put("Triángulo", 81);
        INSTRUMENTS.put("Claves", 75);
        INSTRUMENTS.put("Cencerro", 56);
    }

    public MetronomeEngine(MidiChannel percussionChannel) {
        this.percussionChannel = percussionChannel;
    }

    public void setBpm(int bpm) { this.bpm = bpm; }
    public void setOnTickA(java.util.function.Consumer<Integer> onTickA) { this.onTickA = onTickA; }
    public void setOnTickB(java.util.function.Consumer<Integer> onTickB) { this.onTickB = onTickB; }

    // Setters Caja A
    public void setBeatsA(int beats) { this.beatsA = beats; }
    public void setInstrumentA(String instName) { this.instrumentA = INSTRUMENTS.getOrDefault(instName, 76); }
    public void setVolumeA(int volume) { this.volumeA = volume; }
    public void setMutedA(boolean muted) { this.isMutedA = muted; checkEngineState(); }

    // Setters Caja B
    public void setBeatsB(int beats) { this.beatsB = beats; }
    public void setInstrumentB(String instName) { this.instrumentB = INSTRUMENTS.getOrDefault(instName, 56); }
    public void setVolumeB(int volume) { this.volumeB = volume; }
    public void setMutedB(boolean muted) { this.isMutedB = muted; checkEngineState(); }

    //  Si los dos están muteados, apagamos el motor para ahorrar CPU
    private void checkEngineState() {
        if (!isMutedA || !isMutedB) {
            start();
        } else {
            stop();
        }
    }

    private void start() {
        if (!isRunning) {
            isRunning = true;
            threadA = new Thread(() -> runRhythm(true), "Polyrhythm-A");
            threadB = new Thread(() -> runRhythm(false), "Polyrhythm-B");
            threadA.start();
            threadB.start();
        }
    }

    public void stop() {
        isRunning = false;
        if (threadA != null) threadA.interrupt();
        if (threadB != null) threadB.interrupt();
    }

    private void runRhythm(boolean isRhythm_A) {
        int beatCount = 0;

        while (isRunning) {
            long startTime = System.nanoTime();

            int currentBeats = isRhythm_A ? beatsA : beatsB;
            int currentInstrument = isRhythm_A ? instrumentA : instrumentB;
            int currentVolume = isRhythm_A ? volumeA : volumeB;
            boolean isMuted = isRhythm_A ? isMutedA : isMutedB;

            // Si no está muteado, reproducimos el sonido y encendemos el LED
            // Si no está muteado, reproducimos el sonido y encendemos el LED
            if (!isMuted && percussionChannel != null && currentBeats > 0 && currentVolume > 0) {
                int vol = (beatCount == 0) ? Math.min(127, currentVolume + 20) : currentVolume;
                percussionChannel.noteOn(currentInstrument, vol);

                // NUEVO: Enviamos el número de golpe a la interfaz
                if (isRhythm_A && onTickA != null) onTickA.accept(beatCount);
                if (!isRhythm_A && onTickB != null) onTickB.accept(beatCount);
            }

            beatCount = (beatCount + 1) % Math.max(1, currentBeats);

            long masterCycleMs = (long) ((60000.0 / bpm) * 4);
            long msPerBeat = masterCycleMs / Math.max(1, currentBeats);
            long elapsedMs = (System.nanoTime() - startTime) / 1000000;
            long sleepTime = msPerBeat - elapsedMs;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}