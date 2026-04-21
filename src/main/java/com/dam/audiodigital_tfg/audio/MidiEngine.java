package com.dam.audiodigital_tfg.audio;

import javax.sound.midi.*;
import java.util.concurrent.CompletableFuture;

public class MidiEngine {

    private Synthesizer synthesizer;
    private MidiChannel[] channels;

    public static final int INSTRUMENT_ACOUSTIC_PIANO = 0;
    public static final int INSTRUMENT_ACOUSTIC_GUITAR = 25;

    public MidiEngine() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            channels = synthesizer.getChannels();
            System.out.println("Midi iniciado correctamente");

            connectExternalMidiDevices();
        } catch (MidiUnavailableException e) {
            System.err.println("Error: El hardware MIDI no está disponible.");
            e.printStackTrace();
        }
    }

    public void changeInstrument(int instrumentProgram) {
        if (channels != null && channels.length > 0) {
            channels[0].programChange(instrumentProgram);
            System.out.println("Instrumento cambiado al programa: " + instrumentProgram);
        }
    }

    public void playNote(int noteNumber, int velocity, int durationMs) {
        if (channels == null || channels.length == 0) return;
        CompletableFuture.runAsync(() -> {
            try {
                noteOn(noteNumber, velocity);
                Thread.sleep(durationMs);
                noteOff(noteNumber);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void noteOn(int noteNumber, int velocity) {
        if (channels != null && channels.length > 0) channels[0].noteOn(noteNumber, velocity);
    }

    public void noteOff(int noteNumber) {
        if (channels != null && channels.length > 0) channels[0].noteOff(noteNumber);
    }

    // ¡ESTE ES EL PUENTE HACIA EL METRÓNOMO!
    public MidiChannel getPercussionChannel() {
        if (channels != null && channels.length > 9) return channels[9];
        return null;
    }

    private void connectExternalMidiDevices() {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                if (device.getMaxTransmitters() != 0 && !(device instanceof Synthesizer)) {
                    device.open();
                    Transmitter transmitter = device.getTransmitter();
                    transmitter.setReceiver(new ExternalMidiReceiver());
                    System.out.println("Dispositivo MIDI conectado: " + info.getName());
                }
            } catch (MidiUnavailableException e) {
                System.out.println("No se pudo conectar a: " + info.getName());
            }
        }
    }

    private class ExternalMidiReceiver implements Receiver {
        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int command = sm.getCommand();
                int note = sm.getData1();
                int velocity = sm.getData2();

                if (command == ShortMessage.NOTE_ON) {
                    if (velocity > 0) noteOn(note, velocity);
                    else noteOff(note);
                } else if (command == ShortMessage.NOTE_OFF) {
                    noteOff(note);
                }
            }
        }
        @Override
        public void close() {}
    }

    public void close() {
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
            System.out.println("Midi engine cerrado");
        }
    }
}