package com.dam.audiodigital_tfg.audio;

import javax.sound.midi.MidiChannel;

public class MetronomeEngine implements Runnable {
    private volatile boolean isRunning = false;
    private int bpm = 120;
    private int numerator = 4;
    private int volume = 100; // Por defecto
    private MidiChannel percussionChannel;

    // Ya no crea un Sintetizador, se lo pasan por aquí
    public MetronomeEngine(MidiChannel percussionChannel) {
        this.percussionChannel = percussionChannel;
    }

    public void setBpm(int bpm) { this.bpm = bpm; }
    public void setNumerator(int numerator) { this.numerator = numerator; }
    public void setVolume(int volume) { this.volume = volume; }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            new Thread(this, "Metronome-Thread").start();
        }
    }

    public void stop() { isRunning = false; }

    @Override
    public void run() {
        int beatCount = 0;
        while (isRunning) {
            long startTime = System.nanoTime();

            if (percussionChannel != null) {
                if (beatCount == 0) {
                    percussionChannel.noteOn(76, volume); // Fuerte
                } else {
                    percussionChannel.noteOn(77, Math.max(0, volume - 30)); // Suave
                }
            }

            beatCount = (beatCount + 1) % numerator;
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