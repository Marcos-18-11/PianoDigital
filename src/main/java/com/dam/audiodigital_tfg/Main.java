package com.dam.audiodigital_tfg;

import com.dam.audiodigital_tfg.audio.MidiEngine;

public class Main {
    public static void main(String[] args){

        System.out.println("Inicio de prueba: motor de audio");

        MidiEngine midiEngine=new MidiEngine();

        midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_PIANO);

        // Octava C4

        midiEngine.playNote(60, 100, 500);
        midiEngine.playNote(61, 100, 500);
        midiEngine.playNote(62, 100, 500);
        midiEngine.playNote(63, 100, 500);
        midiEngine.playNote(64, 100, 500);
        midiEngine.playNote(65, 100, 500);
        midiEngine.playNote(66, 100, 500);
        midiEngine.playNote(67, 100, 500);
        midiEngine.playNote(68, 100, 500);
        midiEngine.playNote(69, 100, 500);
        midiEngine.playNote(70, 100, 500);
        midiEngine.playNote(71, 100, 500);

        try {Thread.sleep(500);}catch (Exception ignored){}

        midiEngine.changeInstrument(MidiEngine.INSTRUMENT_ACOUSTIC_GUITAR);

        // Escala do mayor en C4

        midiEngine.playNote(60, 100, 1000);
        midiEngine.playNote(62, 100, 1000);
        midiEngine.playNote(64, 100, 1000); // Mi
        midiEngine.playNote(65, 100, 1000);
        midiEngine.playNote(67, 100, 1000);
        midiEngine.playNote(69, 100, 1000);
        midiEngine.playNote(71, 100, 1000);
        midiEngine.playNote(72, 100, 1000);

        // Escala árabe

        midiEngine.playNote(60, 100, 1000);
        midiEngine.playNote(63, 100, 1000);
        midiEngine.playNote(64, 100, 1000); // Mi
        midiEngine.playNote(65, 100, 1000);
        midiEngine.playNote(67, 100, 1000);
        midiEngine.playNote(70, 100, 1000);
        midiEngine.playNote(71, 100, 1000);
        midiEngine.playNote(72, 100, 1000);

        midiEngine.close();
    }
}
