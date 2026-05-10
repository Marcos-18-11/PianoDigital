package com.dam.audiodigital_tfg.audio;

import javax.sound.midi.MidiChannel;

public class MetronomeEngine {
    private boolean isRunning = false;
    private int bpm = 120;
    private int primaryRhythm = 4;   // Ritmo base (ej. 4)
    private int secondaryRhythm = 0; // Ritmo cruzado (0 = Desactivado, ej. 3 para polirritmo 4:3)
    private int volume = 100;

    private MidiChannel percussionChannel;
    private Thread primaryThread;
    private Thread secondaryThread;

    public MetronomeEngine(MidiChannel percussionChannel) {
        this.percussionChannel = percussionChannel;
    }

    public void setBpm(int bpm) { this.bpm = bpm; }
    public void setPrimaryRhythm(int rhythm) { this.primaryRhythm = rhythm; }
    public void setSecondaryRhythm(int rhythm) { this.secondaryRhythm = rhythm; }
    public void setVolume(int volume) { this.volume = volume; }

    public void start() {
        if (!isRunning) {
            isRunning = true;

            // 1. Lanzamos el hilo del ritmo principal
            primaryThread = new Thread(this::runPrimary, "Metronome-Primary");
            primaryThread.start();

            // 2. Si hay polirritmo, lanzamos el hilo secundario
            if (secondaryRhythm > 0) {
                secondaryThread = new Thread(this::runSecondary, "Metronome-Secondary");
                secondaryThread.start();
            }
        }
    }

    public void stop() {
        isRunning = false; // Esto hará que los bucles while se detengan
    }

    // --- HILO PRINCIPAL (El Woodblock) ---
    private void runPrimary() {
        int beatCount = 0;
        while (isRunning) {
            long startTime = System.nanoTime();

            if (percussionChannel != null) {
                if (beatCount == 0) {
                    percussionChannel.noteOn(76, volume); // Primer golpe fuerte
                } else {
                    percussionChannel.noteOn(77, Math.max(0, volume - 30)); // Golpes débiles
                }
            }

            beatCount = (beatCount + 1) % primaryRhythm;

            // Cálculo del tiempo de espera
            long msPerBeat = (long) (60000.0 / bpm);
            sleepAccurate(msPerBeat, startTime);
        }
    }

    // --- HILO SECUNDARIO (El Polirritmo - Cowbell) ---
    private void runSecondary() {
        while (isRunning) {
            long startTime = System.nanoTime();

            if (percussionChannel != null) {
                // Nota 56 es el Cowbell (Cencerro), ideal para distinguir el polirritmo
                percussionChannel.noteOn(56, Math.max(0, volume - 20));
            }

            // MATEMÁTICA DEL POLIRRITMO:
            // Tiempo total de un compás = (ms por beat principal) * número de beats principales
            // Tiempo por beat secundario = (Tiempo total) / número de beats secundarios
            long totalMeasureMs = (long) (60000.0 / bpm) * primaryRhythm;
            long msPerSecondaryBeat = totalMeasureMs / secondaryRhythm;

            sleepAccurate(msPerSecondaryBeat, startTime);
        }
    }

    // Método auxiliar para esperas precisas restando el tiempo de ejecución
    private void sleepAccurate(long targetMs, long startTimeNano) {
        long elapsedMs = (System.nanoTime() - startTimeNano) / 1000000;
        long sleepTime = targetMs - elapsedMs;

        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}