package com.dam.audiodigital_tfg.audio;

import com.dam.audiodigital_tfg.RecordedNote;

import javax.sound.midi.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MidiEngine {

    private Synthesizer synthesizer;
    private MidiChannel[] channels;
    private CppAudioBridge cppBridge = new CppAudioBridge();

    private boolean isSaturationEnabled = false; // Por defecto apagado
    private float saturationDrive = 3.0f; // La "fuerza" del efecto

    // Controles de Delay
    private boolean isDelayEnabled = false;
    private int delayTimeMs = 350; // Tiempo en milisegundos que tarda el "eco"

    // ================= Variables del Secuenciador =================
    private boolean isRecording = false;
    private long recordStartTime = 0;
    private java.util.List<RecordedNote> recordedNotes = new java.util.ArrayList<>();

    private java.util.Map<Integer, Long> activeNotes = new java.util.HashMap<>();

    // ==========================================
// CONSTANTES DE INSTRUMENTOS (General MIDI)
// ==========================================
    public static final int INSTRUMENT_ACOUSTIC_PIANO = 0;
    public static final int INSTRUMENT_ACOUSTIC_PIANO_ELECTRIC = 5;
    public static final int INSTRUMENT_ACOUSTIC_ORGAN = 19;
    public static final int INSTRUMENT_ACOUSTIC_CHURCH_ORGAN = 20;
    public static final int INSTRUMENT_ACOUSTIC_GUITAR = 25;
    public static final int INSTRUMENT_ACOUSTIC_JAZZ_GUITAR = 27;
    public static final int INSTRUMENT_ACOUSTIC_GELECTRIC_BASS = 34;
    public static final int INSTRUMENT_ACOUSTIC_CONTRABASS = 44;
    public static final int INSTRUMENT_ACOUSTIC_VIOLA = 42;
    public static final int INSTRUMENT_ACOUSTIC_ORCHESTA = 56;
    public static final int INSTRUMENT_ACOUSTIC_CHOIR = 53;
    public static final int INSTRUMENT_ACOUSTIC_TRUMPET = 57;
    public static final int INSTRUMENT_ACOUSTIC_FRENCH_HORN = 61;
    public static final int INSTRUMENT_ACOUSTIC_SAX = 66;
    public static final int INSTRUMENT_ACOUSTIC_FLUTE = 74;
    public static final int INSTRUMENT_ACOUSTIC_TUBULAR_BELLS = 15;
    public static final int INSTRUMENT_ACOUSTIC_SITAR = 105;
    public static final int INSTRUMENT_ACOUSTIC_SINTH_LEAD = 83;
    public static final int INSTRUMENT_ACOUSTIC_WARM_PAD = 90;
    public static final int INSTRUMENT_ACOUSTIC_BRIGHTNESS = 101;
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
    public void setSaturationEnabled(boolean enabled) {
        this.isSaturationEnabled = enabled;
        System.out.println("Efecto de Saturación C++: " + (enabled ? "ENCENDIDO" : "APAGADO"));
    }
    public void changeInstrument(int instrumentProgram) {
        if (channels != null && channels.length > 0) {
            channels[0].programChange(instrumentProgram);
            System.out.println("Instrumento cambiado al programa: " + instrumentProgram);
        }
    }

    // Interfaz para avisar a la UI cuando el hardware mueve un fader
    public interface ControlChangeListener {
        void onControlChange(int ccNumber, int value);
    }

    private ControlChangeListener controlChangeListener;

    public void setControlChangeListener(ControlChangeListener listener) {
        this.controlChangeListener = listener;
    }

    public void setDelayEnabled(boolean enabled) {
        this.isDelayEnabled = enabled;
        System.out.println("⏱️ Efecto Delay: " + (enabled ? "ENCENDIDO" : "APAGADO"));
    }

    // Método para cambiar volumen de un canal específico
    public void setChannelVolume(int channel, int volume) {
        if (channels != null && channels.length > channel) {
            channels[channel].controlChange(7, volume); // CC 7 = Volumen
        }
    }

    // ==========================================
    // IMPORTACIÓN DE RECURSOS EXTERNOS
    // ==========================================
    // ==========================================
    // IMPORTACIÓN DE RECURSOS EXTERNOS (SF2/DLS)
    // ==========================================
    public void loadCustomSoundbank(java.io.File file) {
        try {
            Soundbank customBank = MidiSystem.getSoundbank(file);

            if (synthesizer.isSoundbankSupported(customBank)) {
                synthesizer.loadAllInstruments(customBank);
                Instrument[] instruments = customBank.getInstruments();

                boolean pianoAssigned = false;
                boolean drumsAssigned = false;

                // Recorremos todos los instrumentos que trae el archivo
                for (Instrument inst : instruments) {
                    int bank = inst.getPatch().getBank();
                    int program = inst.getPatch().getProgram();

                    // En el estándar MIDI, el Banco 128 es el de percusión
                    if (bank == 128 && !drumsAssigned) {
                        // Lo asignamos al Canal 9 (Drum Pads)
                        channels[9].programChange(bank, program);
                        drumsAssigned = true;
                        System.out.println("🥁 Batería cargada: " + inst.getName());
                    }
                    // Si no es el banco 128, es un instrumento melódico
                    else if (bank != 128 && !pianoAssigned) {
                        // Lo asignamos al Canal 0 (Piano)
                        channels[0].programChange(bank, program);
                        pianoAssigned = true;
                        System.out.println("🎹 Instrumento melódico cargado: " + inst.getName());
                    }

                    // Si ya hemos encontrado uno de cada, dejamos de buscar
                    if (pianoAssigned && drumsAssigned) break;
                }

                if (!drumsAssigned) {
                    System.out.println("⚠️ Este SoundFont no contiene kits de batería (Banco 128). Los pads mantendrán el sonido por defecto.");
                }

            } else {
                System.err.println("❌ El formato de este banco de sonidos no está soportado por tu SO.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error crítico al leer el archivo de audio: " + file.getName());
            e.printStackTrace();
        }
    }

    // --- MÉTODOS DIRECTOS Y SIN LATENCIA ---
    public void noteOn(int noteNumber, int velocity) {
        // 1. Saturación C++ (Si está activa)
        int finalVelocity = velocity;
        if (isSaturationEnabled) {
            finalVelocity = cppBridge.getSaturatedVelocity(velocity, saturationDrive);
        }

        // 2. Tocamos la nota real
        if (channels != null && channels.length > 0) {
            channels[0].noteOn(noteNumber, finalVelocity);
        }

        // 🚨 3. GRABACIÓN CON TIEMPO RELATIVO 🚨
        if (isRecording) {
            // Guardamos exactamente cuántos milisegundos han pasado desde que le dimos a REC
            long timeElapsed = System.currentTimeMillis() - recordStartTime;
            activeNotes.put(noteNumber, timeElapsed);
        }

        // 4. EFECTO DELAY (El Eco)
        if (isDelayEnabled) {
            final int echoVelocity = finalVelocity / 2;
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(delayTimeMs);
                    if (channels != null && channels.length > 0) {
                        channels[0].noteOn(noteNumber, echoVelocity);
                        Thread.sleep(150);
                        channels[0].noteOff(noteNumber);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public void noteOff(int noteNumber) {
        if (channels != null && channels.length > 0) {
            channels[0].noteOff(noteNumber);

            // 🚨 CERRAMOS LA NOTA Y CALCULAMOS LA DURACIÓN REAL 🚨
            if (isRecording && activeNotes.containsKey(noteNumber)) {
                long relativeStartTime = activeNotes.remove(noteNumber);
                long relativeEndTime = System.currentTimeMillis() - recordStartTime;
                int duration = (int) (relativeEndTime - relativeStartTime);

                // Guardamos la nota usando el tiempo relativo correcto
                recordedNotes.add(new com.dam.audiodigital_tfg.RecordedNote(relativeStartTime, noteNumber, 100, duration, false));
            }
        }
    }

    // Para los Drum Pads (suelen ser golpes cortos o "one-shots", no necesitan duration real)
    // Para los Drum Pads
    public void playPad(int noteNumber, int velocity) {
        MidiChannel percChannel = getPercussionChannel();
        if (percChannel != null) {

            // 1. Saturación C++ (Ideal para bombos y cajas más agresivos)
            int finalVelocity = velocity;
            if (isSaturationEnabled) {
                finalVelocity = cppBridge.getSaturatedVelocity(velocity, saturationDrive);
            }

            // 2. Grabamos el golpe (usamos la velocidad ya saturada)
            if (isRecording) {
                long timeElapsed = System.currentTimeMillis() - recordStartTime;
                recordedNotes.add(new com.dam.audiodigital_tfg.RecordedNote(timeElapsed, noteNumber, finalVelocity, 100, true)); // 100ms fijos
            }

            // 3. Suena el golpe real
            percChannel.noteOn(noteNumber, finalVelocity);

            // 4. Efecto Delay (Eco en la batería)
            if (isDelayEnabled) {
                final int echoVelocity = finalVelocity / 2; // El eco es más suave
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(delayTimeMs);
                        percChannel.noteOn(noteNumber, echoVelocity);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
    }

    // --- PUENTE HACIA EL METRÓNOMO ---
    public MidiChannel getPercussionChannel() {
        if (channels != null && channels.length > 9) return channels[9];
        return null;
    }

    // --- HARDWARE EXTERNO ---
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

    // --- EFECTOS MIDI ESTÁNDAR ---
    public void setReverbEnabled(boolean enabled) {
        if (channels != null) {
            // El nivel MIDI va de 0 a 127. Le ponemos 100 para que se note bastante el "eco"
            int reverbLevel = enabled ? 100 : 0;

            // CC 91 es el estándar universal MIDI para Reverb Depth (Profundidad de Reverb)
            channels[0].controlChange(91, reverbLevel);

            System.out.println("🌊 Efecto Reverb (CC 91): " + (enabled ? "ENCENDIDO" : "APAGADO"));
        }
    }

    // Ajusta la profundidad real del Reverb (0 a 127)
    public void setReverbLevel(int level) {
        if (channels != null && channels.length > 0) {
            channels[0].controlChange(91, level);
            if (channels.length > 9) {
                channels[9].controlChange(91, level); // Reverb para la Batería
            }// CC 91 = Nivel de Reverb
        }
    }

    // Ajusta la fuerza real de la Saturación C++ (0.0f a 10.0f)
    public void setSaturationDrive(float drive) {
        // Multiplicamos por 5 o 10 para que el efecto sea "salvaje"
        // Un drive de 5 en el slider se convertirá en 50 para C++
        this.saturationDrive = drive * 10.0f;

        this.isSaturationEnabled = (drive > 0.1f);

        // Log para confirmar qué le llega a C++
        if(isSaturationEnabled) {
            System.out.println("🔥 Enviando Drive a C++: " + this.saturationDrive);
        }
    }

    private class ExternalMidiReceiver implements Receiver {
        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int command = sm.getCommand();
                int data1 = sm.getData1(); // Nota MIDI o Número de CC
                int velocityOrValue = sm.getData2(); // Fuerza del golpe o Valor del fader
                int channel = sm.getChannel();

                // 1. GESTIÓN DE NOTAS (Teclas y Pads)
                if (command == ShortMessage.NOTE_ON) {
                    if (velocityOrValue > 0) {
                        if (channel == 9) {
                            // ¡CORRECCIÓN 1! Mandamos el golpe a playPad para que SE GRABE
                            playPad(data1, velocityOrValue);
                        } else {
                            // El piano ya pasa por noteOn, que sí sabe grabar
                            noteOn(data1, velocityOrValue);
                        }
                    } else {
                        if (channel == 9 && getPercussionChannel() != null) getPercussionChannel().noteOff(data1);
                        else noteOff(data1);
                    }
                } else if (command == ShortMessage.NOTE_OFF) {
                    if (channel == 9 && getPercussionChannel() != null) getPercussionChannel().noteOff(data1);
                    else noteOff(data1);
                }
                // 2. GESTIÓN DE FADERS Y RULETAS (Control Change)
                else if (command == ShortMessage.CONTROL_CHANGE) {
                    int ccNumber = data1;
                    int ccValue = velocityOrValue;

                    // Solo procesamos si son los faders que nos interesan
                    if (ccNumber == 82 || ccNumber == 83 || ccNumber == 85 || ccNumber == 17) {
                        int targetChannel = (ccNumber == 82) ? 0 : (ccNumber == 83) ? 1 : (ccNumber == 85) ? 2 : 3;

                        // Aplicamos volumen
                        setChannelVolume(targetChannel, ccValue);

                        // Avisamos a la UI para mover los Sliders
                        if (controlChangeListener != null) {
                            javafx.application.Platform.runLater(() ->
                                    controlChangeListener.onControlChange(ccNumber, ccValue));
                        }
                    }
                }
            }
        }

        @Override
        public void close() {}
    }

    // ================= Controles de Grabación =================
    public void startRecording() {
        recordedNotes.clear(); // Limpiamos la grabación anterior
        activeNotes.clear();   // Limpiamos la memoria temporal por seguridad
        recordStartTime = System.currentTimeMillis(); // Marcamos el tiempo cero
        isRecording = true;
        System.out.println("🔴 Grabación iniciada...");
    }

    public void stopRecording() {
        isRecording = false;

        // Cierre de seguridad: Si el usuario le dio a STOP sin soltar alguna tecla,
        // calculamos el tiempo hasta ahora y guardamos esa nota forzosamente.
        long stopTime = System.currentTimeMillis() - recordStartTime;

        for (java.util.Map.Entry<Integer, Long> entry : activeNotes.entrySet()) {
            int note = entry.getKey();
            long startTime = entry.getValue();
            int duration = (int) (stopTime - startTime);

            // La guardamos en la sesión
            recordedNotes.add(new RecordedNote(startTime, note, 100, duration, false));

            // Apagamos el sonido real en el sintetizador para que no se quede pitando
            channels[0].noteOff(note);
        }

        // Vaciamos la memoria temporal
        activeNotes.clear();

        System.out.println("⏹️ Grabación detenida. Notas capturadas en total: " + recordedNotes.size());
    }

    public java.util.List<RecordedNote> getRecordedNotes() {
        return recordedNotes;
    }

    public void playSession(List<RecordedNote> notes, int targetChannel) {
        if (notes.isEmpty()) return;

        // Ejecutamos la reproducción en un hilo nuevo (Multithreading)
        new Thread(() -> {
            System.out.println("▶️ Reproduciendo pista en canal: " + targetChannel);
            long startTime = System.currentTimeMillis();

            for (RecordedNote note : notes) {
                // Calculamos cuánto falta para que deba sonar esta nota
                long timeToWait = note.timestampMs - (System.currentTimeMillis() - startTime);

                if (timeToWait > 0) {
                    try {
                        Thread.sleep(timeToWait);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                // Disparamos la nota en el canal correspondiente
                // Si es batería (is_drum), usamos el canal 9, si no, el targetChannel (0-3)
                int channelIndex = note.isDrum ? 9 : targetChannel;

                CompletableFuture.runAsync(() -> {
                    try {
                        channels[channelIndex].noteOn(note.note, note.velocity);
                        Thread.sleep(note.durationMs);
                        channels[channelIndex].noteOff(note.note);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            System.out.println("⏹️ Fin de la reproducción del canal " + targetChannel);
        }).start();
    }

    public void close() {
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
            System.out.println("Midi engine cerrado");
        }
    }
}