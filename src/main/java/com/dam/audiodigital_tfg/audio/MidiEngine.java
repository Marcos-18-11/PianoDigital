package com.dam.audiodigital_tfg.audio;

import com.dam.audiodigital_tfg.RecordedNote;

import javax.sound.midi.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MidiEngine {

    private com.dam.audiodigital_tfg.MappingManager mappingManager;

    private Synthesizer synthesizer;
    private MidiChannel[] channels;
    private CppAudioBridge cppBridge = new CppAudioBridge();


    private double[] trackVolumeFactors = {1.0, 1.0, 1.0, 1.0, 1.0};

    private boolean isSaturationEnabled = false;    // Por defecto apagado
    private float saturationDrive = 3.0f;           // La "fuerza" del efecto

    // Controles de Delay
    private boolean isDelayEnabled = false;
    private int delayTimeMs = 350; // Tiempo en milisegundos que tarda el eco

    // ================= Variables del Secuenciador =================
    private boolean isRecording = false;
    private long recordStartTime = 0;
    private java.util.List<RecordedNote> recordedNotes = new java.util.ArrayList<>();

    private java.util.Map<Integer, Long> activeNotes = new java.util.HashMap<>();
    // ================= Variables del Mixer (Control de Reproducción) =================
    private java.util.Map<Integer, Thread> hilosReproduccion = new java.util.concurrent.ConcurrentHashMap<>();
    private java.util.Map<Integer, Boolean> reproduciendoCanal = new java.util.concurrent.ConcurrentHashMap<>();

    private int currentInstrument = 0;

        // =======================================
    // CONSTANTES DE INSTRUMENTOS (General MIDI)
        // =======================================
    public static final int INSTRUMENT_ACOUSTIC_PIANO = 0;
    public static final int INSTRUMENT_ACOUSTIC_PIANO_ELECTRIC = 5;
    public static final int INSTRUMENT_ACOUSTIC_ORGAN = 19;
    public static final int INSTRUMENT_ACOUSTIC_CHURCH_ORGAN = 20;
    public static final int INSTRUMENT_ACOUSTIC_GUITAR = 25;
    public static final int INSTRUMENT_ACOUSTIC_JAZZ_GUITAR = 27;
    public static final int INSTRUMENT_ACOUSTIC_ELECTRIC_BASS = 34;
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
        this.currentInstrument = instrumentProgram; // 🚨 NUEVO: Guardamos el estado
        if (channels != null && channels.length > 0) {
            channels[0].programChange(instrumentProgram);
            System.out.println("Instrumento cambiado al programa: " + instrumentProgram);
        }
    }

    // Interfaz para avisar a la UI cuando se mueve un fader
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
    public void setMappingManager(com.dam.audiodigital_tfg.MappingManager mm) {
        this.mappingManager = mm;
    }

    // Listener para avisar a la interfaz de que se ha disparado una acción
    public interface ActionTriggerListener {
        void onActionTriggered(String actionId);
        void onMappingSuccess(); // Para que la interfaz quite el borde amarillo
    }

    private ActionTriggerListener actionListener;

    public void setActionTriggerListener(ActionTriggerListener listener) {
        this.actionListener = listener;
    }
    // Método para cambiar volumen de un canal específico

    public void setChannelVolume(int channel, int volume) {
        if (channel >= 0 && channel < trackVolumeFactors.length) {
            trackVolumeFactors[channel] = volume / 127.0; // Guardamos el % del fader
        }

        if (channels != null && channels.length > channel) {
            channels[channel].controlChange(7, volume); // CC 7 = Volumen general
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

        //  3. GRABACIÓN CON TIEMPO RELATIVO
        if (isRecording) {
            // Guardamos exactamente cuántos milisegundos han pasado desde que le dimos a REC
            long timeElapsed = System.currentTimeMillis() - recordStartTime;
            activeNotes.put(noteNumber, timeElapsed);
        }

        // 4. EFECTO DELAY
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

            //  CERRAMOS LA NOTA Y CALCULAMOS LA DURACIÓN REAL
            if (isRecording && activeNotes.containsKey(noteNumber)) {
                long relativeStartTime = activeNotes.remove(noteNumber);
                long relativeEndTime = System.currentTimeMillis() - recordStartTime;
                int duration = (int) (relativeEndTime - relativeStartTime);

                // Guardamos la nota usando el tiempo relativo correcto
                recordedNotes.add(new com.dam.audiodigital_tfg.RecordedNote(relativeStartTime, noteNumber, 100, duration, false));
            }
        }
    }

    // Para los Drum Pads
    public void playPad(int noteNumber, int velocity) {
        MidiChannel percChannel = getPercussionChannel();
        if (percChannel != null) {

            // 1. Saturación C++
            int finalVelocity = velocity;
            if (isSaturationEnabled) {
                finalVelocity = cppBridge.getSaturatedVelocity(velocity, saturationDrive);
            }

            // 2. Grabamos el golpe
            if (isRecording) {
                long timeElapsed = System.currentTimeMillis() - recordStartTime;
                recordedNotes.add(new com.dam.audiodigital_tfg.RecordedNote(timeElapsed, noteNumber, finalVelocity, 100, true)); // 100ms fijos
            }

            // 3. Suena el golpe real
            percChannel.noteOn(noteNumber, finalVelocity);

            // 4. Efecto Delay
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

            // El nivel MIDI va de 0 a 127.

            int reverbLevel = enabled ? 100 : 0; // 100 por defecto

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
            System.out.println(" Enviando Drive a C++: " + this.saturationDrive);
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

                        //  --- INTERCEPTOR DE MAPEO ---
                        if (mappingManager != null) {
                            // A. Si estamos en modo aprender y esperando una tecla...
                            if (mappingManager.isLearnMode() && mappingManager.getWaitingAction() != null) {
                                mappingManager.mapMidiKey(data1);
                                if (actionListener != null) {
                                    javafx.application.Platform.runLater(() -> actionListener.onMappingSuccess());
                                }
                                return; // No hacemos sonar la nota, solo la registramos
                            }

                            // B. Si estamos en modo normal, comprobamos si la tecla hace algo especial
                            String action = mappingManager.getActionForMidiKey(data1);
                            if (action != null) {
                                if (actionListener != null) {
                                    javafx.application.Platform.runLater(() -> actionListener.onActionTriggered(action));
                                }
                                return; // Ejecuta la acción y no suena la nota
                            }
                        }
                        //  --- FIN INTERCEPTOR ---

                        // Comportamiento normal (Si no está mapeada a nada especial)
                        if (channel == 9) {
                            playPad(data1, velocityOrValue);
                        } else {
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

                        //  EL ARREGLO: Sumamos 1 a cada canal para emparejarlo con el Mixer de la UI
                        // CC 82 -> Canal 1 (Pista 1)
                        // CC 83 -> Canal 2 (Pista 2)
                        // CC 85 -> Canal 3 (Pista 3)
                        // CC 17 -> Canal 4 (Pista 4)
                        int targetChannel = (ccNumber == 82) ? 1 : (ccNumber == 83) ? 2 : (ccNumber == 85) ? 3 : 4;

                        // Cambia el volumen de la pista de reproducción correspondiente
                        setChannelVolume(targetChannel, ccValue);

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

        System.out.println("⏹ Grabación detenida. Notas capturadas en total: " + recordedNotes.size());
    }

    public java.util.List<RecordedNote> getRecordedNotes() {
        return recordedNotes;
    }

    // =======================
    // REPRODUCCIÓN DEL MIXER
    // =======================

    public void stopSessionPlayback(int canal) {
        // 1. Avisamos al bucle para que deje de enviar notas
        reproduciendoCanal.put(canal, false);

        // 2. Buscamos el hilo principal de ese canal y lo interrumpimos
        Thread hilo = hilosReproduccion.get(canal);
        if (hilo != null && hilo.isAlive()) {
            hilo.interrupt();
        }

        // 3. Silenciador de emergencia (All Notes Off)
        // Evita que una nota se quede "pitando" infinitamente si la cortamos a la mitad
        if (channels != null && channels.length > canal) {
            channels[canal].controlChange(123, 0); // Apaga las notas del piano/sinte
            channels[9].controlChange(123, 0);     // Apaga los pads de batería por si acaso
        }
    }

    public void playSession(List<RecordedNote> notes, int targetChannel) {
        if (notes.isEmpty()) return;

        // 1. Si ya había algo sonando en este canal, lo machacamos
        stopSessionPlayback(targetChannel);

        // 2. Marcamos que este canal empieza a sonar (ahora está en bucle)
        reproduciendoCanal.put(targetChannel, true);

        // 3. Creamos el hilo de reproducción
        Thread hiloReproduccion = new Thread(() -> {
            System.out.println("▶️ Reproduciendo pista en BUCLE en canal: " + targetChannel);

            // El bucle while envuelve a toda la reproducción
            while (reproduciendoCanal.getOrDefault(targetChannel, false)) {

                // Tiempo Cero de vuelta concreta
                long startTime = System.currentTimeMillis();

                for (RecordedNote note : notes) {
                    // Si el usuario pulsó STOP, salimos del for
                    if (!reproduciendoCanal.getOrDefault(targetChannel, false)) {
                        break;
                    }

                    long timeToWait = note.timestampMs - (System.currentTimeMillis() - startTime);

                    if (timeToWait > 0) {
                        try {
                            Thread.sleep(timeToWait);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break; // Rompe el sleep si le damos a STOP
                        }
                    }

                    if (!reproduciendoCanal.getOrDefault(targetChannel, false)) break;

                    // Canal 9 si es batería, el targetChannel si es melódico
                    int channelIndex = note.isDrum ? 9 : targetChannel;

                    // Lanzamos la nota
                    new Thread(() -> {
                        try {
                            // Leemos cómo está el fader de ESTA pista concreta
                            double factor = trackVolumeFactors[targetChannel];

                            // Multiplicamos la fuerza original de la nota por el fader
                            int adjustedVelocity = (int) (note.velocity * factor);

                            // Nos aseguramos de que no se salga de los límites MIDI (0-127)
                            adjustedVelocity = Math.max(0, Math.min(127, adjustedVelocity));

                            // Lanzamos la nota con la fuerza ya recortada
                            channels[channelIndex].noteOn(note.note, adjustedVelocity);

                            Thread.sleep(note.durationMs);
                            channels[channelIndex].noteOff(note.note);
                        } catch (InterruptedException e) {
                            channels[channelIndex].noteOff(note.note); // Apagado seguro si se interrumpe
                        }
                    }).start();
                } // --- FIN DEL FOR (Acaba la secuencia) ---

                // Si llegamos aquí y reproduciendoCanal sigue en true, el while
                // volverá arriba, reiniciará el startTime y la canción volverá a sonar.
                // Metemos una micro-pausa de 50ms para suavizar el "salto" del bucle.
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } // --- FIN DEL WHILE ---

            System.out.println("⏹️ Fin del bucle en el canal " + targetChannel);
        });

        // 4. Guardamos el hilo en nuestro diccionario y lo arrancamos
        hilosReproduccion.put(targetChannel, hiloReproduccion);
        hiloReproduccion.start();
    }

    // Nos dice si un canal concreto está sonando ahora mismo
    public boolean isChannelPlaying(int canal) {
        return reproduciendoCanal.getOrDefault(canal, false);
    }

    public void changeChannelInstrument(int canal, int instrumentProgram) {
        if (channels != null && channels.length > canal) {
            channels[canal].programChange(instrumentProgram);
        }
    }

    public int getCurrentInstrument() {
        return currentInstrument;
    }

    public void close() {
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
            System.out.println("Midi engine cerrado");
        }
    }
}