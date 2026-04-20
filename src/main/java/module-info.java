module com.dam.audiodigital_tfg {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens com.dam.audiodigital_tfg to javafx.fxml;
    exports com.dam.audiodigital_tfg;
}