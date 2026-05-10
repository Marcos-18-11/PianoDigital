package com.dam.audiodigital_tfg.audio;

import com.dam.audiodigital_tfg.RecordedNote;

import javax.sound.midi.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MidiEngine {

    private Synthesizer synthesizer;
    private MidiChannel[] channels;

    // ================= Variables del Secuenciador =================
    private boolean isRecording = false;
    private long recordStartTime = 0;
    private java.util.List<RecordedNote> recordedNotes = new java.util.ArrayList<>();

    private java.util.Map<Integer, Long> activeNotes = new java.util.HashMap<>();

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
        if (channels != null && channels.length > 0) {
            channels[0].noteOn(noteNumber, velocity);

            // Si estamos grabando, apuntamos en qué milisegundo se empezó a pulsar
            if (isRecording) {
                long timeElapsed = System.currentTimeMillis() - recordStartTime;
                activeNotes.put(noteNumber, timeElapsed);
            }
        }
    }

    public void noteOff(int noteNumber) {
        if (channels != null && channels.length > 0) {
            channels[0].noteOff(noteNumber);

            // Si estamos grabando y teníamos esta nota registrada como pulsada...
            if (isRecording && activeNotes.containsKey(noteNumber)) {
                long startTime = activeNotes.remove(noteNumber); // La sacamos de la memoria temporal
                long endTime = System.currentTimeMillis() - recordStartTime;
                int duration = (int) (endTime - startTime); // ¡Calculamos la duración real!

                // Guardamos la nota completada en la sesión
                recordedNotes.add(new RecordedNote(startTime, noteNumber, 100, duration, false));
            }
        }
    }

    // Para los Drum Pads (suelen ser golpes cortos o "one-shots", no necesitan duration real)
    public void playPad(int noteNumber, int velocity) {
        if (getPercussionChannel() != null) {
            if (isRecording) {
                long timeElapsed = System.currentTimeMillis() - recordStartTime;
                recordedNotes.add(new RecordedNote(timeElapsed, noteNumber, velocity, 100, true)); // 100ms fijos
            }
            getPercussionChannel().noteOn(noteNumber, velocity);
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

                    // Mantenemos el log para confirmar que todo va fino
                    System.out.println("🎚️ Hardware -> CC: " + ccNumber + " | Valor: " + ccValue);

                    // MAPEO REAL DE TU MINILAB:
                    // Usamos el CC 7, que es el estándar MIDI universal para el Volumen de Canal.
                    if (ccNumber == 82) { // Tu primer fader
                        channels[0].controlChange(7, ccValue);
                    } else if (ccNumber == 83) { // Tu segundo fader
                        channels[1].controlChange(7, ccValue);
                    } else if (ccNumber == 85) { // Tu tercer fader
                        channels[2].controlChange(7, ccValue);
                    } else if (ccNumber == 17) { // Tu cuarto fader/knob
                        channels[3].controlChange(7, ccValue);
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