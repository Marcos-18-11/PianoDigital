package com.dam.audiodigital_tfg.audio;

public class CppAudioBridge {

    static {
        try {
            // Carga la librería compilada (audio_dsp.dll)
            System.loadLibrary("audio_dsp");
            System.out.println("Aaudio_dsp cargado");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Error: No se pudo cargar la librería nativa C++.");
        }
    }

    // Método nativo (el que está en el .cpp)
    private native int applySaturation(int velocity, float drive);

    /**
     * Llama al motor de C++ para aplicar saturación a la velocidad MIDI.
     * @param velocity Valor original (0-127)
     * @param drive Nivel de distorsión (0.0 a 10.0)
     * @return Nueva velocidad procesada
     */
    public int getSaturatedVelocity(int velocity, float drive) {
        try {
            return applySaturation(velocity, drive);
        } catch (Throwable t) {
            // Si algo falla en el puente, devolvemos la nota limpia
            return velocity;
        }
    }
}