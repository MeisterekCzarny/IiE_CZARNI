package com.example.silowniaprojekt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        // Ustawienie stylu i ikon
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("login_styles.css").toExternalForm());

        // Ustawienie ikony aplikacji
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.avif")));
        } catch (Exception e) {
            System.err.println("Nie udało się załadować ikony aplikacji: " + e.getMessage());
        }

        primaryStage.setTitle("Black Iron Gym - System logowania");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}