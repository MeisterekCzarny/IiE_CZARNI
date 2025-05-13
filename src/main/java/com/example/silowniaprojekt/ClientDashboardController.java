package com.example.silowniaprojekt;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ClientDashboardController {

    // Sekcje
    @FXML private VBox homeSection;
    @FXML private VBox profileSection;
    @FXML private VBox membershipSection;
    @FXML private VBox trainingsSection;
    @FXML private VBox shopSection;
    @FXML private VBox scheduleSection;

    // Panel profilu
    @FXML private Label emailLabel;
    @FXML private Label expiryLabel;
    @FXML private Label statusLabel;
    @FXML private Label completedTrainingsLabel;
    @FXML private Label attendanceLabel;
    @FXML private Label progressLabel;
    @FXML private Label heightLabel;
    @FXML private Label waistLabel;
    @FXML private Label flakLabel;
    @FXML private Label weightLabel;
    @FXML private Label muscleMassLabel;
    @FXML private Label hydrationLabel;

    // Panel sklepu
    @FXML private Label cartCounterLabel;

    // Panel harmonogramu
    @FXML private TableView<ScheduleEntry> scheduleTable;
    @FXML private TableColumn<ScheduleEntry, String> timeColumn;
    @FXML private TableColumn<ScheduleEntry, String> mondayColumn;
    @FXML private TableColumn<ScheduleEntry, String> tuesdayColumn;
    @FXML private TableColumn<ScheduleEntry, String> wednesdayColumn;
    @FXML private TableColumn<ScheduleEntry, String> thursdayColumn;
    @FXML private TableColumn<ScheduleEntry, String> fridayColumn;
    @FXML private TableColumn<ScheduleEntry, String> saturdayColumn;

    // Dane użytkownika
    private int userId;
    private String userName;
    private String userEmail;
    private UserProfile userProfile;

    // Koszyk
    private int cartItemCount = 0;

    @FXML
    public void initialize() {
        // Zainicjalizuj interfejs
        initializeProfile();
        initializeSchedule();

        // Ustaw sekcję domową jako aktywną
        showHome();
    }

    public void setUserData(int userId, String userName, String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;

        // Załaduj dane użytkownika z bazy
        loadUserData();

        // Zaktualizuj interfejs
        updateProfileUI();
    }

    private void loadUserData() {
        // Stwórz przykładowy profil użytkownika (w produkcji załadowałbyś go z bazy)
        userProfile = new UserProfile();
        userProfile.email = userEmail;
        userProfile.expiryDate = "01.05.2025";
        userProfile.status = "AKTYWNY (PRO)";
        userProfile.completedTrainings = 21;
        userProfile.attendance = 98;
        userProfile.progress = 12;
        userProfile.height = 180;
        userProfile.waist = 80;
        userProfile.skinFold = 20;
        userProfile.weight = 89;
        userProfile.muscleMass = 68;
        userProfile.hydration = 87;

        // Tutaj możesz dodać kod do pobrania danych z bazy
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                String query = "SELECT * FROM user_profiles WHERE user_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Jeśli znaleziono dane w bazie, zaktualizuj profil
                    // userProfile.height = rs.getInt("height");
                    // itd.
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania danych użytkownika: " + e.getMessage());
        }
    }

    private void updateProfileUI() {
        // Zaktualizuj elementy UI danymi użytkownika
        emailLabel.setText(userProfile.email);
        expiryLabel.setText(userProfile.expiryDate);
        statusLabel.setText(userProfile.status);
        completedTrainingsLabel.setText(String.valueOf(userProfile.completedTrainings));
        attendanceLabel.setText(userProfile.attendance + "%");
        progressLabel.setText(userProfile.progress + "kg");
        heightLabel.setText(userProfile.height + "cm");
        waistLabel.setText(userProfile.waist + "cm");
        flakLabel.setText(userProfile.skinFold + "cm");
        weightLabel.setText(userProfile.weight + "KG");
        muscleMassLabel.setText(userProfile.muscleMass + "KG");
        hydrationLabel.setText(userProfile.hydration + "%");
    }

    private void initializeProfile() {
        // Inicjalizacja domyślnymi wartościami, zanim załadujemy prawdziwe dane
        emailLabel.setText("użytkownik@example.com");
        expiryLabel.setText("XX.XX.XXXX");
        statusLabel.setText("NIEAKTYWNY");
        completedTrainingsLabel.setText("0");
        attendanceLabel.setText("0%");
        progressLabel.setText("0kg");
        heightLabel.setText("0cm");
        waistLabel.setText("0cm");
        flakLabel.setText("0cm");
        weightLabel.setText("0KG");
        muscleMassLabel.setText("0KG");
        hydrationLabel.setText("0%");
    }

    private void initializeSchedule() {
        // Inicjalizacja kolumn tabeli
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        mondayColumn.setCellValueFactory(new PropertyValueFactory<>("monday"));
        tuesdayColumn.setCellValueFactory(new PropertyValueFactory<>("tuesday"));
        wednesdayColumn.setCellValueFactory(new PropertyValueFactory<>("wednesday"));
        thursdayColumn.setCellValueFactory(new PropertyValueFactory<>("thursday"));
        fridayColumn.setCellValueFactory(new PropertyValueFactory<>("friday"));
        saturdayColumn.setCellValueFactory(new PropertyValueFactory<>("saturday"));

        // Dodaj przykładowe dane
        ObservableList<ScheduleEntry> scheduleData = FXCollections.observableArrayList(
                new ScheduleEntry("7:00 - 8:00", "CrossFit", "Yoga", "TRX", "Mobility", "CrossFit", "Yoga"),
                new ScheduleEntry("8:00 - 9:00", "Strongman", "-", "CrossFit", "TRX", "-", "CrossFit"),
                new ScheduleEntry("9:00 - 10:00", "TRX", "CrossFit", "-", "CrossFit", "Mobility", "-"),
                new ScheduleEntry("18:00 - 19:00", "Yoga", "CrossFit", "Mobility", "-", "TRX", "CrossFit"),
                new ScheduleEntry("19:00 - 20:00", "CrossFit", "TRX", "CrossFit", "Yoga", "CrossFit", "-"),
                new ScheduleEntry("20:00 - 21:00", "Mobility", "CrossFit", "Yoga", "CrossFit", "-", "-")
        );

        scheduleTable.setItems(scheduleData);

        // Możesz dodać kod do pobrania rzeczywistego harmonogramu z bazy danych
    }

    // Metody przełączania widoków
    @FXML
    public void showHome() {
        hideAllSections();
        homeSection.setVisible(true);
    }

    @FXML
    public void showProfile() {
        hideAllSections();
        profileSection.setVisible(true);
    }

    @FXML
    public void showMembership() {
        hideAllSections();
        membershipSection.setVisible(true);
    }

    @FXML
    public void showTrainings() {
        hideAllSections();
        trainingsSection.setVisible(true);
    }

    @FXML
    public void showShop() {
        hideAllSections();
        shopSection.setVisible(true);
    }

    @FXML
    public void showSchedule() {
        hideAllSections();
        scheduleSection.setVisible(true);
    }

    private void hideAllSections() {
        homeSection.setVisible(false);
        profileSection.setVisible(false);
        membershipSection.setVisible(false);
        trainingsSection.setVisible(false);
        shopSection.setVisible(false);
        scheduleSection.setVisible(false);
    }

    // Metody do obsługi karnetów
    @FXML
    public void addBasicToCart() {
        showConfirmDialog("Karnet BASIC", "Czy chcesz dodać do koszyka karnet BASIC za 150 zł?");
    }

    @FXML
    public void addProToCart() {
        showConfirmDialog("Karnet PRO", "Czy chcesz dodać do koszyka karnet PRO za 400 zł?");
    }

    @FXML
    public void addDailyToCart() {
        showConfirmDialog("Karnet DZIENNY", "Czy chcesz dodać do koszyka karnet DZIENNY za 25 zł?");
    }

    @FXML
    public void addMonthlyToCart() {
        showConfirmDialog("Karnet MIESIĘCZNY", "Czy chcesz dodać do koszyka karnet MIESIĘCZNY za 180 zł?");
    }

    @FXML
    public void addYearlyToCart() {
        showConfirmDialog("Karnet ROCZNY", "Czy chcesz dodać do koszyka karnet ROCZNY za 1200 zł?");
    }

    // Metody do obsługi sklepu
    @FXML
    public void showCart() {
        if (cartItemCount == 0) {
            showAlert("Koszyk", "Twój koszyk jest pusty.");
        } else {
            showAlert("Koszyk", "Masz " + cartItemCount + " produkt(ów) w koszyku.");
        }
    }

    @FXML
    public void buyWheyProtein() {
        addToCart("WHEY PROTEIN COMPLEX", 129.0);
    }

    @FXML
    public void buyCreatine() {
        addToCart("Kreatyna monohydrat", 35.0);
    }

    @FXML
    public void buyShaker() {
        addToCart("Shaker Wielkiego Chłopa", 29.0);
    }

    @FXML
    public void buySyringe() {
        addToCart("Strzykawka", 6.0);
    }

    @FXML
    public void buyTrembolone() {
        addToCart("Trembolon", 165.0);
    }

    @FXML
    public void buyNandrolone() {
        addToCart("Nandrolon", 200.0);
    }

    @FXML
    public void buyWeight1() {
        addToCart("Drostanolon", 125.0);
    }

    @FXML
    public void buyWeight2() {
        addToCart("Dihydroboldenone (DHB)", 150.0);
    }

    @FXML
    public void buyWeight3() {
        addToCart("Testosterone", 135.0);
    }

    private void addToCart(String productName, double price) {
        cartItemCount++;
        cartCounterLabel.setText("(" + cartItemCount + ")");
        showAlert("Koszyk", "Dodano do koszyka: " + productName + " - " + price + " zł");
    }

    private void showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cartItemCount++;
            cartCounterLabel.setText("(" + cartItemCount + ")");
            showAlert("Koszyk", "Dodano do koszyka: " + title);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Metoda do wylogowania
    @FXML
    public void logout() {
        try {
            // Zamknij bieżące okno
            Stage stage = (Stage) homeSection.getScene().getWindow();

            // Otwórz ekran logowania
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Black Iron Gym - Logowanie");
            loginStage.setScene(new Scene(root, 400, 300));
            loginStage.show();

            // Zamknij bieżące okno
            stage.close();
        } catch (IOException e) {
            showAlert("Błąd", "Nie udało się wrócić do ekranu logowania: " + e.getMessage());
        }
    }

    // Klasa do przechowywania danych profilu użytkownika
    private class UserProfile {
        String email;
        String expiryDate;
        String status;
        int completedTrainings;
        int attendance;
        int progress;
        int height;
        int waist;
        int skinFold;
        int weight;
        int muscleMass;
        int hydration;
    }

    // Klasa do obsługi wpisów w harmonogramie
    public static class ScheduleEntry {
        private final String time;
        private final String monday;
        private final String tuesday;
        private final String wednesday;
        private final String thursday;
        private final String friday;
        private final String saturday;

        public ScheduleEntry(String time, String monday, String tuesday, String wednesday,
                             String thursday, String friday, String saturday) {
            this.time = time;
            this.monday = monday;
            this.tuesday = tuesday;
            this.wednesday = wednesday;
            this.thursday = thursday;
            this.friday = friday;
            this.saturday = saturday;
        }

        // Gettery dla właściwości
        public String getTime() { return time; }
        public String getMonday() { return monday; }
        public String getTuesday() { return tuesday; }
        public String getWednesday() { return wednesday; }
        public String getThursday() { return thursday; }
        public String getFriday() { return friday; }
        public String getSaturday() { return saturday; }
    }
}