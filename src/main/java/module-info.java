module com.dam.audiodigital_tfg {
    // REQUIRES: Define las librerías externas que el DAW necesita para funcionar.
    requires javafx.controls; // Para los botones, sliders y la ventana.
    requires javafx.fxml;     // Para cargar las vistas diseñadas en Scene Builder.
    requires java.desktop;    // OBLIGATORIO: Aquí reside la API javax.sound.midi.
    requires java.sql;        // Para la futura gestión de la base de datos SQLite.

    // OPENS: Da permiso a JavaFX para que "entre" en nuestro paquete.
    // Sin esto, JavaFX no podría conectar los botones de la vista con el código.
    opens com.dam.audiodigital_tfg to javafx.fxml;

    // EXPORTS: Permite que otros módulos vean nuestras clases.
    exports com.dam.audiodigital_tfg;
    exports com.dam.audiodigital_tfg.audio;
}