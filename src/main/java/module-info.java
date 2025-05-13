module com.example.silowniaprojekt {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;
    requires kernel;
    requires layout;

    opens com.example.silowniaprojekt to javafx.fxml;
    exports com.example.silowniaprojekt;
}