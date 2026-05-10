package com.dam.audiodigital_tfg;

public class RecordedNote {
    public long timestampMs;
    public int note;
    public int velocity;
    public int durationMs;
    public boolean isDrum;

    public RecordedNote(long timestampMs, int note, int velocity, int durationMs, boolean isDrum) {
        this.timestampMs = timestampMs;
        this.note = note;
        this.velocity = velocity;
        this.durationMs = durationMs;
        this.isDrum = isDrum;
    }
}