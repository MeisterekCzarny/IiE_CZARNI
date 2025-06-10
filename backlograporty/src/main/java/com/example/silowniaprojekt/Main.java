package com.example.silowniaprojekt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Główna klasa aplikacji siłowni Black Iron Gym.
 * Inicjalizuje aplikację JavaFX, ładuje ekran logowania
 * i konfiguruje podstawowe ustawienia interfejsu użytkownika.
 */
public class Main extends Application {

    /**
     * Inicjalizuje i uruchamia aplikację siłowni.
     *
     * @param primaryStage główne okno aplikacji
     * @throws Exception w przypadku błędu ładowania zasobów FXML lub CSS
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Załaduj plik FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        // Pobierz kontroler i przekaż referencję do Stage
        LoginController controller = loader.getController();
        controller.setLoginStage(primaryStage);

        // Utwórz scenę i dodaj styl CSS
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("login_styles.css").toExternalForm());

        // Ustaw ikonę aplikacji
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.avif")));
        } catch (Exception e) {
            System.err.println("Błąd ładowania ikony: " + e.getMessage());
        }

        // Konfiguracja okna
        primaryStage.setTitle("Black Iron Gym - System logowania");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);         // 🔄 Użytkownik może zmieniać rozmiar
        primaryStage.setMaximized(true);         // 🖥️ Rozciągnij na cały ekran, ale nie ukrywaj paska zadań
        primaryStage.show();
    }

    /**
     * Punkt wejścia do aplikacji.
     *
     * @param args argumenty wiersza poleceń
     */
    public static void main(String[] args) {
        launch(args);
    }
}
