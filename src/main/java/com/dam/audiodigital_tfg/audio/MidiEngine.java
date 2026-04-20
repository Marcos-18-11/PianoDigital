package com.dam.audiodigital_tfg.audio;

import javax.sound.midi.*;
import java.util.concurrent.CompletableFuture;


public class MidiEngine {

    private Synthesizer synthesizer;
    private MidiChannel[] channels;

    public  static final int INSTRUMENT_ACOUSTIC_PIANO=0;
    public  static final int INSTRUMENT_ACOUSTIC_GUITAR=25;

    public MidiEngine(){

        try{

        synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();

        channels = synthesizer.getChannels();

            System.out.println("Midi iniciado correctamente");

        } catch (MidiUnavailableException e) {
            System.err.println("Error: El hardware MIDI no está disponible.");
            e.printStackTrace();
        }
    }


    /**
     * @param instrumentProgram El número de programa MIDI (0-127)
     */

    public void changeInstrument(int instrumentProgram){

        if(channels != null && channels.length>0) {
            channels[0].programChange(instrumentProgram);
            System.out.println("Instrumento cambiado al programa: " + instrumentProgram);

        }

    }
    /*
    * @param noteNumber número de nota MIDI -> Ej 60: do C4
    * @param velocity Fuerza con la que se toca la nota
    * @param durationMs Duración en milisegundos
    * */

    public void playNote(int noteNumber, int velocity, int durationMs){

        if (channels == null || channels.length == 0) return;{

            CompletableFuture.runAsync(()->{
                try{

                    channels[0].noteOn(noteNumber, velocity);
                    Thread.sleep(0);
                    channels[0].noteOff(noteNumber);

                } catch(InterruptedException e) {
                    System.err.println("Reproducción de nota interrumpida");
                    Thread.currentThread().interrupt();
                }
            });


            /*
            *   try{
                channels[0].noteOn(noteNumber, velocity);
                Thread.sleep(durationMs);
                channels[0].noteOff(noteNumber);
            } catch (InterruptedException e) {
                System.err.println("Reproduccion de nota interrumpida");
                Thread.currentThread().interrupt();
            }
            * */

        }
    }
    // cerrar y liberar recursos
    public void close(){
        if(synthesizer != null && synthesizer.isOpen()){
            synthesizer.close();
            System.out.println("Midi engine cerrado");
        }
    }
}
