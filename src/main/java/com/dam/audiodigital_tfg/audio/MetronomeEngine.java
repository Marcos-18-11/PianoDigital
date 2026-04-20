package com.dam.audiodigital_tfg.audio;

import javax.sound.midi.*;

public class MetronomeEngine implements Runnable {
    private boolean isRunning = false;
    private int bpm = 120;
    private int numerator = 4; // Compás de 4/4 por defecto
    private Synthesizer synth;
    private MidiChannel percussionChannel;

    public MetronomeEngine() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            // El canal 9 (índice 9 de un array de 0 a 15) es el canal de percusión estándar en MIDI
            percussionChannel = synth.getChannels()[9];
        } catch (MidiUnavailableException e) {
            System.err.println("Error al cargar el sintetizador MIDI: " + e.getMessage());
        }
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            new Thread(this, "Metronome-Thread").start();
        }
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        int beatCount = 0;

        while (isRunning) {
            long startTime = System.nanoTime();

            // Lógica de sonido: El primer golpe es fuerte (Woodblock alto), los demás son suaves
            if (beatCount == 0) {
                percussionChannel.noteOn(76, 127); // Nota 76, Velocidad (Volumen) Máxima
            } else {
                percussionChannel.noteOn(77, 90);  // Nota 77, Velocidad Media
            }

            beatCount = (beatCount + 1) % numerator;

            // Calcular cuánto debe esperar este hilo según los BPM
            long msPerBeat = (long) (60000.0 / bpm);
            long sleepTime = msPerBeat - ((System.nanoTime() - startTime) / 1000000);

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}