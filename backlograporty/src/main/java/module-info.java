module com.example.silowniaprojekt {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;
    requires kernel;
    requires layout;
    requires java.desktop;
    requires io;
    requires gym.reports.library;


    opens com.example.silowniaprojekt to javafx.fxml;
    exports com.example.silowniaprojekt;
}