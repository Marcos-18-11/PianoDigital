-- Tabla para guardar configuraciones del Metrónomo
CREATE TABLE IF NOT EXISTS metronome_presets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    bpm INTEGER DEFAULT 120,
    ratio_a INTEGER DEFAULT 4,
    ratio_b INTEGER DEFAULT 4
);

-- Tabla para definir diferentes Kits de Batería (Rock, HipHop, Jazz...)
CREATE TABLE IF NOT EXISTS drum_kits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

-- Tabla para los mapeos de cada Pad dentro de un Kit
CREATE TABLE IF NOT EXISTS drum_mappings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    kit_id INTEGER,
    pad_index INTEGER, -- Del 0 al 15 (para tu rejilla 4x4)
    midi_note INTEGER, -- La nota que disparará (36=Kick, 38=Snare, etc.)
    label TEXT,        -- Nombre visual (ej. "BOMBO PROFUNDO")
    FOREIGN KEY (kit_id) REFERENCES drum_kits(id) ON DELETE CASCADE
);

-- Tabla para sesiones de ensayo (Setlists)
CREATE TABLE IF NOT EXISTS setlists (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);