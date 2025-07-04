package com.example.silowniaprojekt;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Kontroler panelu pracownika siłowni.
 * Zarządza interfejsem użytkownika dla pracownika, umożliwiając:
 * - rejestrację i zarządzanie użytkownikami
 * - zarządzanie sprzętem siłowni
 * - obsługę cennika i karnetów
 * - przetwarzanie transakcji
 * - obsługę harmonogramu treningów
 * - przeglądanie raportów
 * 
 * Klasa zapewnia pełną funkcjonalność panelu pracownika, z podziałem na sekcje
 * odpowiadające różnym obszarom zarządzania siłownią.
 */
/**
 * Kontroler panelu pracownika siłowni.
 * Zarządza funkcjami dostępnymi dla pracownika, takimi jak zarządzanie użytkownikami,
 * sprzętem, płatnościami za karnety, produktami, harmonogramami, żądaniami treningów
 * i transakcjami.
 * Implementuje logikę dla wszystkich działań dostępnych w interfejsie panelu pracownika.
 */
public class EmployeeDashboardController {

    private int currentUserId;
    private String currentUserEmail;

    /**
     * Tworzy nowy kontroler panelu pracownika.
     * Konstruktor domyślny wywoływany przez system JavaFX podczas ładowania widoku FXML.
     */

    // Sekcje
    @FXML
    private VBox dashboardSection;
    @FXML
    private VBox registrationSection;
    @FXML
    private VBox pricingSection;
    @FXML
    private VBox transactionsSection;

    @FXML
    private VBox scheduleSection;
    @FXML
    private VBox membershipsSection; // NEW: Sekcja Karnety

    @FXML private Button dashboardBtn;
    @FXML private Button registrationBtn;
    @FXML private Button transactionsBtn;

    @FXML private Button membershipsBtn;
    @FXML private Button scheduleBtn;

    @FXML private TextField transactionQuantityField;
    @FXML private Label productPriceLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label transactionAmountLabel;


    // Etykiety błędów (dodaj je w FXML pod odpowiednimi polami)
    @FXML private Label nameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;

    private void setActiveNavButton(Button activeBtn) {
        dashboardBtn.getStyleClass().remove("active");
        registrationBtn.getStyleClass().remove("active");
        transactionsBtn.getStyleClass().remove("active");
        membershipsBtn.getStyleClass().remove("active");
        scheduleBtn.getStyleClass().remove("active");
        if (activeBtn != null && !activeBtn.getStyleClass().contains("active")) {
            activeBtn.getStyleClass().add("active");
        }
    }

    // Rejestracja
    /**
     * Pole tekstowe do wprowadzania imienia i nazwiska klienta podczas rejestracji.
     * Wykorzystywane w formularzu rejestracji nowych użytkowników.
     */
    @FXML
    public TextField clientNameField;

    @FXML
    private ComboBox<User> transactionClientComboBox;

    private ObservableList<TrainingRequestEntry> trainingRequestsScheduleData = FXCollections.observableArrayList();


    @FXML private TableView<TrainingRequestEntry> scheduleTable; // Zmieniony typ TableView
    @FXML private TableColumn<TrainingRequestEntry, Integer> trainingRequestIdColumn; // Nowa kolumna
    @FXML private TableColumn<TrainingRequestEntry, String> scheduleClientColumn; // Nowa kolumna
    @FXML private TableColumn<TrainingRequestEntry, String> scheduleTrainerColumn; // Pozostaje, ale typ zmieniony na TrainingRequestEntry
    @FXML private TableColumn<TrainingRequestEntry, LocalDateTime> scheduleDateTimeColumn; // Nowa kolumna dla daty i czasu
    @FXML private TableColumn<TrainingRequestEntry, String> scheduleNotesColumn; // Nowa kolumna dla notatek
    @FXML private TableColumn<TrainingRequestEntry, String> scheduleStatusColumn; // Pozostaje, ale typ zmieniony na TrainingRequestEntry

    @FXML private TableColumn<Transaction, String> transactionProductNameColumn;
    // ...
    @FXML private ComboBox<Product> transactionProductComboBox;
    // Cennik
    @FXML
    private TableView<PricingPackage> pricingTable;
    @FXML
    private TableColumn<PricingPackage, String> membershipNameColumn;
    @FXML
    private TableColumn<PricingPackage, String> membershipDescColumn;
    @FXML
    private TableColumn<PricingPackage, Double> membershipPriceColumn;
    @FXML
    private TableColumn<PricingPackage, String> membershipDurationColumn;
    @FXML
    private TableColumn<PricingPackage, String> membershipStatusColumn;
    @FXML
    private TextField packageNameField;
    @FXML
    private TextField packagePriceField;
    @FXML
    private ComboBox<String> packageDurationCombo;
    private ObservableList<PricingPackage> pricingData = FXCollections.observableArrayList();

    // Transakcje (UPDATED)
    @FXML
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, Integer> transactionIdColumn;
    @FXML
    private TableColumn<Transaction, String> transactionClientColumn;
    @FXML
    private TableColumn<Transaction, BigDecimal> transactionAmountColumn;
    @FXML
    private TableColumn<Transaction, Timestamp> transactionDateColumn;

    private ObservableList<Transaction> transactionData = FXCollections.observableArrayList();

    // Raporty
    @FXML
    private Label revenueLabel;
    @FXML
    private Label revenueChangeLabel;
    @FXML
    private Label newClientsLabel;
    @FXML
    private Label clientsChangeLabel;
    @FXML
    private Label attendanceLabel;
    @FXML
    private Label attendanceChangeLabel;



    @FXML private TableView<TrainingRequestPayment> trainingRequestPaymentsTable;
    @FXML private TableColumn<TrainingRequestPayment, Integer> paymentIdColumn;
    @FXML private TableColumn<TrainingRequestPayment, Timestamp> paymentDateColumn;
    @FXML private TableColumn<TrainingRequestPayment, BigDecimal> paymentAmountColumn;
    @FXML private TextField paymentAmountField;
    private ObservableList<TrainingRequestPayment> trainingRequestPaymentsData = FXCollections.observableArrayList();

    // NEW: Karnety Section
    @FXML
    private TableView<MembershipPayment> membershipsTable;
    @FXML
    private TableColumn<MembershipPayment, Integer> membershipIdColumn;
    @FXML
    private TableColumn<MembershipPayment, String> membershipClientColumn; // Will display client name
    @FXML
    private TableColumn<MembershipPayment, BigDecimal> membershipAmountColumn;
    @FXML
    private TableColumn<MembershipPayment, Timestamp> membershipPaymentDateColumn;
    @FXML
    private ComboBox<User> membershipClientComboBox; // Use User model for client selection
    @FXML
    private TextField membershipAmountField;
    private ObservableList<MembershipPayment> membershipPaymentsData = FXCollections.observableArrayList();

    // Pola do rejestracji
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> userNameColumn;
    @FXML
    private TableColumn<User, String> userEmailColumn;
    @FXML
    private TableColumn<User, String> userRoleColumn;

    @FXML
    private TableColumn<User, String> userStatusColumn;
    /**
     * Pole tekstowe do wprowadzania adresu email podczas rejestracji użytkownika.
     * Wykorzystywane w formularzu rejestracji i edycji danych użytkownika.
     */
    @FXML
    public TextField emailField;
    /**
     * Pole do wprowadzania hasła podczas rejestracji nowego użytkownika.
     * Wykorzystywane w formularzu rejestracji dla zapewnienia bezpieczeństwa konta.
     */
    @FXML
    public PasswordField passwordField;
    /**
     * Lista rozwijana do wyboru roli użytkownika (klient, trener, pracownik).
     * Umożliwia określenie poziomu dostępu i uprawnień użytkownika w systemie.
     */
    @FXML
    public ComboBox<String> roleComboBox;


    /**
     * Obserwowalna lista przechowująca wszystkich użytkowników systemu.
     * Wykorzystywana do wyświetlania danych w tabeli użytkowników i zarządzania nimi.
     */
    public ObservableList<User> users = FXCollections.observableArrayList();
    // private ObservableList<Schedule> scheduleData = FXCollections.observableArrayList(); // Ta lista już nie jest potrzebna, bo masz trainingRequestsScheduleData

    /**
     * Inicjalizuje wszystkie komponenty interfejsu użytkownika.
     * Metoda wywoływana automatycznie przez JavaFX po załadowaniu pliku FXML.
     */
    @FXML
    public void initialize() {
        initializeRegistration();
        initializeTransactions();
        initializeSchedule(); // Ta metoda zostanie zmodyfikowana, aby ustawić listener
        initializeMemberships();
        initializeTrainingRequestPayments(); // Zapewnia inicjalizację tabeli płatności
        hideAllSections();
        showDashboard();
    }

    /**
     * Inicjalizuje tabelę płatności za treningi.
     * Konfiguruje kolumny tabeli i przypisuje dane z kolekcji.
     */
    private void initializeTrainingRequestPayments() {
        if (trainingRequestPaymentsTable != null) { // Dodaj zabezpieczenie null check
            paymentIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
            paymentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            trainingRequestPaymentsTable.setItems(trainingRequestPaymentsData);
        }
    }
    
    /**
     * Ładuje płatności dla określonego żądania treningu.
     * 
     * @param trainingRequestId Identyfikator żądania treningu, dla którego mają zostać załadowane płatności
     */
    private void loadTrainingRequestPayments(int trainingRequestId) {
        trainingRequestPaymentsData.clear();
        String sql = "SELECT id, payment_date, amount FROM training_request_payments WHERE training_request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trainingRequestId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                trainingRequestPaymentsData.add(new TrainingRequestPayment(
                        rs.getInt("id"),
                        rs.getTimestamp("payment_date"),
                        rs.getBigDecimal("amount")
                ));
            }
        } catch (SQLException e) {
            showError("Błąd ładowania płatności za trening: " + e.getMessage());
            e.printStackTrace(); // Ważne, aby zobaczyć pełny stack trace
        }
    }
    
    /**
     * Dodaje nową płatność dla wybranego treningu.
     * Waliduje dane wejściowe i zapisuje płatność do bazy danych.
     */
    @FXML
    private void addTrainingRequestPayment() {
        String amountText = paymentAmountField.getText();
        if (amountText.isEmpty()) {
            showError("Wprowadź kwotę płatności.");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Kwota musi być większa od zera.");
                return;
            }

            TrainingRequestEntry selectedTrainingRequest = scheduleTable.getSelectionModel().getSelectedItem();
            if (selectedTrainingRequest == null) {
                showError("Wybierz trening, dla którego chcesz dodać płatność.");
                return;
            }
            int trainingRequestId = selectedTrainingRequest.getRequestId();

            String sql = "INSERT INTO training_request_payments (training_request_id, payment_date, amount) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, trainingRequestId);
                pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                pstmt.setBigDecimal(3, amount);
                pstmt.executeUpdate();

                showAlert("Sukces", "Płatność została dodana.");
                loadTrainingRequestPayments(trainingRequestId); // Odśwież listę płatności dla wybranego treningu
                paymentAmountField.clear();

            } catch (SQLException e) {
                showError("Błąd dodawania płatności: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            showError("Nieprawidłowy format kwoty. Użyj formatu liczbowego (np. 150.00).");
        }
    }

    /**
     * Inicjalizuje sekcję karnetów członkowskich.
     * Konfiguruje kolumny tabeli, ładuje płatności i klientów do comboboxa.
     */
    private void initializeMemberships() {
        if (membershipsTable != null) {
            membershipIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            membershipClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
            membershipAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            membershipPaymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
            membershipsTable.setItems(membershipPaymentsData);
        }

        // Kwota
        membershipAmountColumn.setCellFactory(column -> new TableCell<MembershipPayment, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f zł", item));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                }
            }
        });

        // Data
        membershipPaymentDateColumn.setCellFactory(column -> new TableCell<MembershipPayment, Timestamp>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                }
            }
        });

        loadMembershipPayments();
        loadClientsForMembershipComboBox();
    }

    /**
     * Inicjalizuje sekcję rejestracji użytkowników.
     * Konfiguruje combobox z rolami, ładuje użytkowników do tabeli.
     */
    private void initializeRegistration() {
        roleComboBox.getItems().addAll("client", "trainer", "employee");
        loadUsers();
    
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userStatusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String status = user.getIsActive() ? "Aktywny" : "Nieaktywny";
            return new SimpleStringProperty(status);
        });
        usersTable.setItems(users);
    }
    
    /**
     * Ładuje listę użytkowników z bazy danych do tabeli.
     */
    private void loadUsers() {
        users.clear();
        String query = "SELECT id, name, email, role, is_active FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("id");
                String role = rs.getString("role");

                // Pomijanie aktualnie zalogowanego użytkownika
                if (userId == currentUserId) {
                    continue;
                }

                // Wyświetlaj tylko klientów
                if ("client".equals(role)) {
                    users.add(new User(
                            userId,
                            rs.getString("name"),
                            rs.getString("email"),
                            role,
                            rs.getBoolean("is_active")
                    ));


                }
            }
        } catch (SQLException e) {
            showError("Błąd ładowania użytkowników: " + e.getMessage());
        }
    }
    
    /**
     * Dodaje nowego użytkownika do systemu.
     * Waliduje dane wejściowe i zapisuje użytkownika do bazy danych.
     */
    @FXML
    private void addUser() {
        // Pobierz wartości z formularza
        String name = clientNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        // Resetuj komunikaty błędów
        nameError.setText("");
        emailError.setText("");
        passwordError.setText("");

        boolean isValid = true;

        // Walidacja imienia i nazwiska
        if (name.isEmpty()) {
            nameError.setText("Pole wymagane");
            isValid = false;
        } else if (name.matches(".*\\d.*")) {
            nameError.setText("Nie może zawierać cyfr");
            isValid = false;
        }

        // Walidacja emaila
        if (email.isEmpty()) {
            emailError.setText("Pole wymagane");
            isValid = false;
        } else if (!email.matches(".+@.+\\..+")) {
            emailError.setText("Nieprawidłowy format email");
            isValid = false;
        }

        // Walidacja hasła
        if (password.isEmpty()) {
            passwordError.setText("Pole wymagane");
            isValid = false;
        } else if (password.length() < 8) {
            passwordError.setText("Hasło musi mieć min. 8 znaków");
            isValid = false;
        }

        // Walidacja roli
        if (role == null) {
            showAlert("Błąd", "Wybierz rolę użytkownika");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Sprawdź unikalność emaila
            String checkQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    emailError.setText("Email już istnieje w systemie");
                    return;
                }
            }

            // Dodaj użytkownika
            String insertQuery = "INSERT INTO users (name, email, password, role, is_active) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setString(2, email);

                // Hashowanie hasła
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                stmt.setString(3, hashedPassword);

                stmt.setString(4, role);
                stmt.setBoolean(5, true);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    // Pobierz ID nowego użytkownika
                    ResultSet rs = stmt.getGeneratedKeys();
                    int newId = -1;
                    if (rs.next()) {
                        newId = rs.getInt(1);
                    }

                    // Dodaj do listy
                    users.add(new User(newId, name, email, role, true));
                    usersTable.refresh();

                    // Wyczyść formularz
                    clientNameField.clear();
                    emailField.clear();
                    passwordField.clear();
                    roleComboBox.getSelectionModel().clearSelection();

                    showAlert("Sukces", "Użytkownik został dodany.");
                }
            }
        } catch (SQLException e) {
            showAlert("Błąd bazy danych", "Nie udało się dodać użytkownika: " + e.getMessage());
        }
    }

    @FXML
    public void editUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Wybierz użytkownika do edycji!");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edytuj użytkownika");

        TextField nameField = new TextField(selectedUser.getName());
        TextField emailField = new TextField(selectedUser.getEmail());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Pozostaw puste, aby nie zmieniać");
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("client", "trainer", "employee", "admin"));
        roleCombo.setValue(selectedUser.getRole());

        // Etykiety błędów
        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red;");
        Label emailError = new Label();
        emailError.setStyle("-fx-text-fill: red;");
        Label passwordError = new Label();
        passwordError.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.addRow(0, new Label("Imię i nazwisko:"), nameField);
        grid.add(nameError, 1, 1);
        grid.addRow(2, new Label("Email:"), emailField);
        grid.add(emailError, 1, 3);
        grid.addRow(4, new Label("Nowe hasło (opcjonalnie):"), passwordField);
        grid.add(passwordError, 1, 5);
        grid.addRow(6, new Label("Rola:"), roleCombo);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Początkowo przycisk OK jest wyłączony
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Walidacja w czasie rzeczywistym
        Runnable validateFields = () -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();

            nameError.setText("");
            emailError.setText("");
            passwordError.setText("");

            boolean valid = true;

            if (name.isEmpty()) {
                nameError.setText("Pole wymagane");
                valid = false;
            } else if (name.matches(".*\\d.*")) {
                nameError.setText("Nie może zawierać cyfr");
                valid = false;
            }

            if (email.isEmpty()) {
                emailError.setText("Pole wymagane");
                valid = false;
            } else if (!email.matches(".+@.+\\..+")) {
                emailError.setText("Nieprawidłowy format email");
                valid = false;
            }

            if (!password.isEmpty() && password.length() < 8) {
                passwordError.setText("Min. 8 znaków");
                valid = false;
            }

            okButton.setDisable(!valid);
        };

        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new User(
                        selectedUser.getId(),
                        nameField.getText(),
                        emailField.getText(),
                        roleCombo.getValue()
                );
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(editedUser -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Sprawdź unikalność emaila (poza obecnym użytkownikiem)
                String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE email = ? AND id <> ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailQuery)) {
                    checkStmt.setString(1, editedUser.getEmail());
                    checkStmt.setInt(2, selectedUser.getId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showError("Email już istnieje w systemie!");
                        return;
                    }
                }

                // Aktualizuj użytkownika
                String query;
                PreparedStatement stmt;

                if (passwordField.getText().isEmpty()) {
                    query = "UPDATE users SET name = ?, email = ?, role = ? WHERE id = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, editedUser.getName());
                    stmt.setString(2, editedUser.getEmail());
                    stmt.setString(3, editedUser.getRole());
                    stmt.setInt(4, selectedUser.getId());
                } else {
                    query = "UPDATE users SET name = ?, email = ?, password = ?, role = ? WHERE id = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, editedUser.getName());
                    stmt.setString(2, editedUser.getEmail());

                    // Hashowanie nowego hasła
                    String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());
                    stmt.setString(3, hashedPassword);

                    stmt.setString(4, editedUser.getRole());
                    stmt.setInt(5, selectedUser.getId());
                }

                if (stmt.executeUpdate() > 0) {
                    loadUsers();
                    showAlert("Sukces", "Dane użytkownika zaktualizowane!");
                }
            } catch (SQLException e) {
                showError("Błąd aktualizacji: " + e.getMessage());
            }
        });
    }
    public void setCurrentUser(int userId, String email) {
        this.currentUserId = userId;
        this.currentUserEmail = email;

        System.out.println("Zalogowany pracownik: ID=" + userId + ", Email=" + email);
    }
    /**
     * Usuwa wybranego użytkownika z systemu.
     * Wyświetla okno potwierdzenia przed usunięciem.
     */
    @FXML
    private void deleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Wybierz użytkownika do deaktywacji!");
            return;
        }

        // Zabezpieczenie przed dezaktywacją samego siebie
        if (selectedUser.getId() == currentUserId ||
                (selectedUser.getEmail() != null && selectedUser.getEmail().equals(currentUserEmail))) {
            showError("Operacja niedozwolona: nie możesz dezaktywować własnego konta");
            return;
        }

        // Zabezpieczenie przed dezaktywacją nie-klientów
        if (!"client".equals(selectedUser.getRole())) {
            showError("Operacja niedozwolona: możesz dezaktywować tylko klientów");
            return;
        }

        // Potwierdzenie dezaktywacji
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potwierdzenie");
        confirm.setHeaderText("Czy na pewno chcesz dezaktywować użytkownika " + selectedUser.getName() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE users SET is_active = FALSE WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedUser.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedUser.setIsActive(false);
                    usersTable.refresh();
                    showAlert("Sukces", "Użytkownik został dezaktywowany!");
                }
            } catch (SQLException e) {
                showError("Błąd dezaktywacji: " + e.getMessage());
            }
        }
    }
    @FXML
    private void activateUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Wybierz użytkownika do aktywacji!");
            return;
        }

        // Zabezpieczenie przed operacją na własnym koncie
        if (selectedUser.getId() == currentUserId ||
                (selectedUser.getEmail() != null && selectedUser.getEmail().equals(currentUserEmail))) {
            showError("Operacja niedozwolona: nie możesz zarządzać własnym kontem");
            return;
        }

        // Zabezpieczenie przed aktywacją nie-klientów
        if (!"client".equals(selectedUser.getRole())) {
            showError("Operacja niedozwolona: możesz aktywować tylko klientów");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potwierdzenie");
        confirm.setHeaderText("Czy na pewno chcesz aktywować użytkownika " + selectedUser.getName() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE users SET is_active = TRUE WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedUser.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedUser.setIsActive(true);
                    usersTable.refresh();
                    showAlert("Sukces", "Użytkownik został aktywowany!");
                }
            } catch (SQLException e) {
                showError("Błąd aktywacji: " + e.getMessage());
            }
        }
    }
    


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Inicjalizuje sekcję magazynu.
     * Konfiguruje kolumny tabeli, dodaje obsługę przycisku usuwania sprzętu
     * i ładuje przykładowe dane.
     */
    
    /**
     * Inicjalizuje sekcję cennika.
     * Konfiguruje combobox z okresami, ustawia kolumny tabeli
     * i dodaje przykładowe pakiety cenowe.
     */


    // UPDATED initializeTransactions method
    private void initializeTransactions() {
        if (transactionsTable != null) {
            transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            transactionClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
            transactionProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
            transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
            transactionsTable.setItems(transactionData);
            productPriceLabel.setText("");
            totalPriceLabel.setText("");
            transactionAmountLabel.setText("");


            // Kwota
            transactionAmountColumn.setCellFactory(column -> new TableCell<Transaction, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f zł", item));  // Formatowanie do dwóch miejsc po przecinku
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                    }
                }
            });

            // Data
            transactionDateColumn.setCellFactory(column -> new TableCell<Transaction, Timestamp>() {
                @Override
                protected void updateItem(Timestamp item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());  // Możesz sformatować datę jak chcesz
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                    }
                }
            });

            // Listenery do automatycznego obliczania ceny
            transactionProductComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    productPriceLabel.setText(String.format("Cena: %.2f zł", newVal.getPrice()));
                } else {
                    productPriceLabel.setText("");
                }
                calculateTotalAmount();
            });

            transactionQuantityField.textProperty().addListener((obs, oldVal, newVal) -> {
                calculateTotalAmount();
            });
        }

        loadTransactions();
        loadClientsForTransactionComboBox();
        loadProductsForTransactionComboBox();
    }
    private void calculateTotalAmount() {
        Product selectedProduct = transactionProductComboBox.getValue();
        String quantityText = transactionQuantityField.getText();

        if (selectedProduct == null || quantityText.isEmpty()) {
            totalPriceLabel.setText("");
            transactionAmountLabel.setText("");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                totalPriceLabel.setText("Ilość musi być > 0");
                transactionAmountLabel.setText("");
                return;
            }

            double total = selectedProduct.getPrice() * quantity;
            totalPriceLabel.setText(String.format("Razem: %.2f zł", total));
            transactionAmountLabel.setText(String.format("%.2f zł", total));
        } catch (NumberFormatException e) {
            totalPriceLabel.setText("Nieprawidłowa ilość");
            transactionAmountLabel.setText("");
        }
    }


    // Zmodyfikowana metoda initializeSchedule()
    private void initializeSchedule() {
        if (scheduleTable != null) {
            trainingRequestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
            scheduleClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
            scheduleTrainerColumn.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
            scheduleNotesColumn.setCellValueFactory(new PropertyValueFactory<>("requestNotes"));
            scheduleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            scheduleDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("trainingDateTime"));
            scheduleDateTimeColumn.setCellFactory(column -> new TableCell<TrainingRequestEntry, LocalDateTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Oczekujący");
                        setStyle(null);
                    } else {
                        setText(formatter.format(item));
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                    }
                }
            });

            scheduleTable.setItems(trainingRequestsScheduleData);

            // KLUCZOWY LISTENER: Ładuje płatności po wyborze treningu
            scheduleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadTrainingRequestPayments(newSelection.getRequestId());
                } else {
                    // Jeśli nic nie jest wybrane, wyczyść listę płatności
                    trainingRequestPaymentsData.clear();
                }
            });
        }
    }

    private void loadAllTrainingRequests() {
        trainingRequestsScheduleData.clear();
        String sql = "SELECT tr.id AS request_id, " +
                "c.name AS client_name, " +
                "t.name AS trainer_name, " +
                "tr.training_date, " +
                "r.notes AS request_notes " +
                "FROM trainingrequests tr " +
                "JOIN reports r ON tr.report = r.id " +
                "JOIN users c ON r.client_id = c.id " +
                "JOIN users t ON r.trainer_id = t.id " +
                "ORDER BY tr.training_date DESC, tr.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int requestId = rs.getInt("request_id");
                String clientName = rs.getString("client_name");
                String trainerName = rs.getString("trainer_name");
                Timestamp trainingTimestamp = rs.getTimestamp("training_date");
                LocalDateTime trainingDateTime = (trainingTimestamp != null) ? trainingTimestamp.toLocalDateTime() : null;
                String requestNotes = rs.getString("request_notes");

                String status = (trainingDateTime != null) ? "Zaplanowany" : "Oczekujący";

                trainingRequestsScheduleData.add(new TrainingRequestEntry(
                        requestId,
                        clientName,
                        trainerName,
                        trainingDateTime,
                        requestNotes,
                        status
                ));
            }
        } catch (SQLException e) {
            showError("Błąd ładowania próśb o treningi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Dodaje nowy sprzęt do bazy.
     * Waliduje format liczby i dodaje sprzęt do tabeli.
     */

    
    /**
     * Dodaje nowy pakiet cenowy.
     * Waliduje dane wejściowe i dodaje pakiet do tabeli.
     */
    @FXML
    private void addPricingPackage() {
        try {
            if (packageNameField.getText().isEmpty() || packagePriceField.getText().isEmpty()) {
                showError("Wypełnij wszystkie pola!");
                return;
            }
    
            PricingPackage newPackage = new PricingPackage(
                    packageNameField.getText(),
                    "",
                    Double.parseDouble(packagePriceField.getText()),
                    packageDurationCombo.getValue(),
                    "Aktywny"
            );
            pricingData.add(newPackage);
            clearPricingFields();
        } catch (NumberFormatException e) {
            showError("Nieprawidłowy format ceny!");
        }
    }
    @FXML
    private void logout() {
        try {
            // Pobierz aktualny Stage (okno)
            Stage stage = (Stage) dashboardSection.getScene().getWindow();

            // Załaduj widok logowania
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Ustaw scenę logowania na tym samym oknie
            stage.setTitle("Black Iron Gym - System logowania");
            stage.setScene(new Scene(root, 400, 300));
            stage.setResizable(false);

            // (opcjonalnie) przekaz kontrolerowi Stage, jeśli potrzebujesz
            LoginController controller = loader.getController();
            controller.setLoginStage(stage);

            // NIE twórz nowego Stage, NIE wywołuj stage.close()
            // stage.show(); // niepotrzebne, bo okno już jest widoczne
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Dodaje nową transakcję.
     * Waliduje dane wejściowe, zapisuje transakcję do bazy danych
     * i aktualizuje stan magazynowy produktu.
     */
    @FXML
    private void addTransaction() {
        User selectedClient = transactionClientComboBox.getSelectionModel().getSelectedItem();
        Product selectedProduct = transactionProductComboBox.getSelectionModel().getSelectedItem();
        String quantityText = transactionQuantityField.getText();

        if (selectedClient == null || selectedProduct == null || quantityText.isEmpty()) {
            showError("Wybierz klienta, produkt i podaj ilość!");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showError("Ilość musi być większa od zera.");
                return;
            }

            if (selectedProduct.getStock() < quantity) {
                showError("Wybrany produkt (" + selectedProduct.getName() + ") jest niedostępny w żądanej ilości.");
                return;
            }

            BigDecimal amount = BigDecimal.valueOf(selectedProduct.getPrice() * quantity);

            String sql = "INSERT INTO transactions (client_id, product_id, transaction_date, amount) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedClient.getId());
                pstmt.setInt(2, selectedProduct.getId());
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setBigDecimal(4, amount);
                pstmt.executeUpdate();

                String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setInt(2, selectedProduct.getId());
                    updateStmt.executeUpdate();
                }

                showAlert("Sukces", "Transakcja została dodana. Stan magazynowy zaktualizowany.");
                clearTransactionFields();
                loadTransactions();
                loadProductsForTransactionComboBox();
            }
        } catch (NumberFormatException e) {
            showError("Ilość musi być liczbą całkowitą.");
        } catch (SQLException e) {
            showError("Błąd dodawania transakcji: " + e.getMessage());
            e.printStackTrace();
        }
    }




    private void hideAllSections() {
        dashboardSection.setVisible(false);
        registrationSection.setVisible(false);
        pricingSection.setVisible(false);
        transactionsSection.setVisible(false);
        scheduleSection.setVisible(false);
        membershipsSection.setVisible(false);
    }

    private void toggleSection(VBox section) {
        hideAllSections();
        section.setVisible(true);
    }

    private void clearPricingFields() {
        packageNameField.clear();
        packagePriceField.clear();
        packageDurationCombo.getSelectionModel().clearSelection();
    }

    private void clearTransactionFields() {
        transactionClientComboBox.getSelectionModel().clearSelection();
        transactionProductComboBox.getSelectionModel().clearSelection();
        transactionQuantityField.clear();
        productPriceLabel.setText("");
        totalPriceLabel.setText("");
        transactionAmountLabel.setText("");
    }



    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Wyświetla sekcję pulpitu głównego.
     * Przełącza interfejs na widok głównego pulpitu z podsumowaniem informacji.
     */
    @FXML private void showDashboard() {
        toggleSection(dashboardSection);
        setActiveNavButton(dashboardBtn);
    }

    /**
     * Wyświetla sekcję rejestracji użytkowników.
     * Przełącza interfejs na formularz rejestracji nowych użytkowników.
     */
    @FXML private void showRegistration() {
        toggleSection(registrationSection);
        setActiveNavButton(registrationBtn);
    }

    /**
     * Wyświetla sekcję magazynu.
     * Przełącza interfejs na zarządzanie sprzętem siłowni.
     */

    /**
     * Wyświetla sekcję cennika.
     * Przełącza interfejs na zarządzanie pakietami cenowymi i karnetami.
     */

    /**
     * Wyświetla sekcję transakcji.
     * Przełącza interfejs na zarządzanie transakcjami, ładuje dane transakcji z bazy,
     * wypełnia listy klientów i produktów dostępnych do wyboru.
     */
    @FXML private void showTransactions() {
        toggleSection(transactionsSection);
        setActiveNavButton(transactionsBtn);
        loadTransactions();
        loadClientsForTransactionComboBox();
        loadProductsForTransactionComboBox();
    }
    


    /**
     * Wyświetla sekcję harmonogramu treningów.
     * Przełącza interfejs na zarządzanie harmonogramem, ładuje wszystkie żądania treningów z bazy.
     * Automatycznie wybiera pierwszy wpis z listy treningów (jeśli istnieje) i wyświetla 
     * powiązane z nim płatności.
     */
    @FXML
    private void showSchedule() {
        toggleSection(scheduleSection);
        setActiveNavButton(scheduleBtn);
        loadAllTrainingRequests();

        // Po załadowaniu wszystkich próśb, automatycznie wybierz pierwszy, jeśli istnieje
        // i załaduj jego płatności.
        if (!trainingRequestsScheduleData.isEmpty()) {
            scheduleTable.getSelectionModel().selectFirst();
            // Listener w initializeSchedule() zajmie się załadowaniem płatności dla wybranego elementu.
        } else {
            // Jeśli nie ma żadnych treningów, wyczyść tabelę płatności
            trainingRequestPaymentsData.clear();
        }
    }

    /**
     * Obsługuje zdarzenie wyboru elementu w tabeli harmonogramu treningów.
     * Metoda wywoływana przy zmianie zaznaczenia w tabeli harmonogramu.
     * Odpowiada za aktualizację interfejsu użytkownika w oparciu o wybrany trening.
     * Wykorzystywana głównie przez listener w metodzie initializeSchedule().
     */

    
    
    /**
     * Wyświetla sekcję karnetów członkowskich w interfejsie użytkownika.
     * 
     * Przełącza interfejs na zarządzanie karnetami, ładuje dane płatności za karnety
     * z bazy danych oraz wypełnia listę klientów dostępnych do wyboru.
     */
    /**
     * Wyświetla sekcję karnetów członkowskich w interfejsie użytkownika.
     * Metoda przełącza widoczność na sekcję karnetów, ładuje dane o płatnościach 
     * za członkostwa z bazy danych oraz wypełnia listę rozwijaną klientów 
     * do wyboru przy dodawaniu nowych płatności za karnety.
     * Jest wywoływana po kliknięciu przycisku "Karnety" w panelu nawigacji.
     */
    @FXML
    private void showMemberships() {
        toggleSection(membershipsSection);
        setActiveNavButton(membershipsBtn);
        loadMembershipPayments();
        loadClientsForMembershipComboBox();
    }

    
    /**
     * Ładuje dane transakcji z bazy danych do tabeli transakcji.
     * Metoda pobiera informacje o transakcjach, łącząc tabele transactions, users i products,
     * a następnie wypełnia tabelę w interfejsie użytkownika.
     * Dane są wyświetlane z informacjami o kliencie, produkcie, kwocie i dacie transakcji.
     */
    private void loadTransactions() {
        transactionData.clear();
        String sql = "SELECT t.id, t.client_id, u.name AS client_name, " +
                "t.product_id, p.name AS product_name, t.amount, t.transaction_date " +
                "FROM transactions t " +
                "JOIN users u ON t.client_id = u.id " +
                "JOIN products p ON t.product_id = p.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                transactionData.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("client_name"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("transaction_date")
                ));
            }
        } catch (SQLException e) {
            showError("Błąd ładowania transakcji: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void loadProductsForTransactionComboBox() {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        String sql = "SELECT id, name, price, stock FROM products";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
            transactionProductComboBox.setItems(productList);
        } catch (SQLException e) {
            showError("Błąd ładowania produktów dla transakcji: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadClientsForTransactionComboBox() {
        ObservableList<User> clientList = FXCollections.observableArrayList();
        String sql = "SELECT id, name FROM users WHERE role = 'client'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                clientList.add(new User(rs.getInt("id"), rs.getString("name"), null, null));
            }
            transactionClientComboBox.setItems(clientList);
        } catch (SQLException e) {
            showError("Błąd ładowania klientów dla transakcji: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ładuje płatności za karnety z bazy danych.
     * Czyści i uzupełnia listę płatności na podstawie danych z bazy.
     */
    private void loadMembershipPayments() {
        membershipPaymentsData.clear();
        String sql = "SELECT mp.id, mp.client_id, u.name AS client_name, mp.amount, mp.payment_date " +
                "FROM membership_payments mp JOIN users u ON mp.client_id = u.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                membershipPaymentsData.add(new MembershipPayment(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("client_name"),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("payment_date")
                ));
            }
        } catch (SQLException e) {
            showError("Błąd ładowania płatności za karnety: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ładuje list�� klientów do comboboxa karnetu.
     * Pobiera klientów z bazy danych i wypełnia combobox.
     */
    private void loadClientsForMembershipComboBox() {
        ObservableList<User> clientList = FXCollections.observableArrayList();
        String sql = "SELECT id, name FROM users WHERE role = 'client'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                clientList.add(new User(rs.getInt("id"), rs.getString("name"), null, null));
            }
            membershipClientComboBox.setItems(clientList);
        } catch (SQLException e) {
            showError("Błąd ładowania klientów do wyboru karnetu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Dodaje nową płatność za karnet.
     * Waliduje dane wejściowe, zapisuje płatność do bazy danych i odświeża listę płatności.
     */
    @FXML
    private void addMembershipPayment() {
        User selectedClient = membershipClientComboBox.getSelectionModel().getSelectedItem();
        String amountText = membershipAmountField.getText();

        if (selectedClient == null || amountText.isEmpty()) {
            showError("Wybierz klienta i podaj kwotę płatności za karnet.");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Kwota płatności musi być większa od zera.");
                return;
            }

            String sql = "INSERT INTO membership_payments (client_id, amount, payment_date) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedClient.getId());
                pstmt.setBigDecimal(2, amount);
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.executeUpdate();

                showAlert("Sukces", "Płatność za karnet została dodana.");
                membershipAmountField.clear();
                membershipClientComboBox.getSelectionModel().clearSelection();
                loadMembershipPayments();
            }
        } catch (NumberFormatException e) {
            showError("Kwota musi być poprawną liczbą (np. 99.99).");
        } catch (SQLException e) {
            showError("Błąd dodawania płatności za karnet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Klasy modelowe - UPEWNIJ SIĘ, ŻE ZNAJDUJĄ SIĘ NA KOŃCU PLIKU

    /**
     * Klasa reprezentująca sprzęt w siłowni.
     * Zawiera dane o nazwie, ilości, statusie i dacie ostatniego przeglądu.
     */
    /**
     * Klasa reprezentująca sprzęt w siłowni.
     * Przechowuje informacje o sprzęcie, takie jak identyfikator, nazwa,
     * producent, data zakupu, cena i stan.
     * Implementuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class Equipment {
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty quantity;
        private final SimpleStringProperty status;
        private final SimpleStringProperty lastCheck;

        /**
         * Tworzy nowy obiekt sprzętu.
         *
         * @param name Nazwa sprzętu
         * @param quantity Ilość
         * @param status Status sprzętu (np. "Dostępne", "W naprawie")
         * @param lastCheck Data ostatniego przeglądu
         */
        public Equipment(String name, int quantity, String status, String lastCheck) {
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.status = new SimpleStringProperty(status);
            this.lastCheck = new SimpleStringProperty(lastCheck);
        }

        /**
         * Zwraca nazwę sprzętu.
         * @return Nazwa sprzętu
         */
        public String getName() { return name.get(); }

        /**
         * Zwraca właściwość nazwy sprzętu.
         * @return Właściwość nazwy
         */
        public SimpleStringProperty nameProperty() { return name; }

        /**
         * Zwraca ilość sprzętu.
         * @return Ilość sprzętu
         */
        public int getQuantity() { return quantity.get(); }

        /**
         * Zwraca właściwość ilości sprzętu.
         * @return Właściwość ilości
         */
        public SimpleIntegerProperty quantityProperty() { return quantity; }

        /**
         * Zwraca status sprzętu.
         * @return Status sprzętu
         */
        public String getStatus() { return status.get(); }

        /**
         * Zwraca właściwość statusu sprzętu.
         * @return Właściwość statusu
         */
        public SimpleStringProperty statusProperty() { return status; }

        /**
         * Zwraca datę ostatniego przeglądu.
         * @return Data ostatniego przeglądu
         */
        public String getLastCheck() { return lastCheck.get(); }

        /**
         * Zwraca właściwość daty ostatniego przeglądu.
         * @return Właściwość daty przeglądu
         */
        public SimpleStringProperty lastCheckProperty() { return lastCheck; }
    }

    /**
     * Klasa reprezentująca pakiet cenowy (karnet).
     * Zawiera dane o nazwie, opisie, cenie, okresie ważności i statusie.
     */
    /**
     * Klasa reprezentująca pakiet cenowy (karnet) w siłowni.
     * Przechowuje informacje o pakiecie, takie jak identyfikator, nazwa,
     * opis, cena, okres ważności i dostępne usługi.
     * Implementuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class PricingPackage {
        private final SimpleStringProperty name;
        private final SimpleStringProperty description;
        private final SimpleDoubleProperty price;
        private final SimpleStringProperty duration;
        private final SimpleStringProperty status;

        /**
         * Tworzy nowy pakiet cenowy.
         *
         * @param name Nazwa pakietu
         * @param description Opis pakietu
         * @param price Cena pakietu
         * @param duration Okres ważności (np. "1 miesiąc")
         * @param status Status pakietu (np. "Aktywny")
         */
        public PricingPackage(String name, String description, double price, String duration, String status) {
            this.name = new SimpleStringProperty(name);
            this.description = new SimpleStringProperty(description);
            this.price = new SimpleDoubleProperty(price);
            this.duration = new SimpleStringProperty(duration);
            this.status = new SimpleStringProperty(status);
        }

        /**
         * Zwraca nazwę pakietu.
         * @return Nazwa pakietu
         */
        public String getName() { return name.get(); }

        /**
         * Zwraca właściwość nazwy pakietu.
         * @return Właściwość nazwy
         */
        public SimpleStringProperty nameProperty() { return name; }

        /**
         * Zwraca opis pakietu.
         * @return Opis pakietu
         */
        public String getDescription() { return description.get(); }

        /**
         * Zwraca właściwość opisu pakietu.
         * @return Właściwość opisu
         */
        public SimpleStringProperty descriptionProperty() { return description; }

        /**
         * Zwraca cenę pakietu.
         * @return Cena pakietu
         */
        public double getPrice() { return price.get(); }

        /**
         * Zwraca właściwość ceny pakietu.
         * @return Właściwość ceny
         */
        public SimpleDoubleProperty priceProperty() { return price; }

        /**
         * Zwraca okres ważności pakietu.
         * @return Okres ważności
         */
        public String getDuration() { return duration.get(); }

        /**
         * Zwraca właściwość okresu ważności pakietu.
         * @return Właściwość okresu ważności
         */
        public SimpleStringProperty durationProperty() { return duration; }

        /**
         * Zwraca status pakietu.
         * @return Status pakietu
         */
        public String getStatus() { return status.get(); }

        /**
         * Zwraca właściwość statusu pakietu.
         * @return Właściwość statusu
         */
        public SimpleStringProperty statusProperty() { return status; }
    }

    /**
     * Klasa reprezentująca transakcję w systemie.
     * Zawiera dane o kliencie, produkcie, kwocie i dacie transakcji.
     */
    /**
     * Klasa reprezentująca transakcję w systemie siłowni.
     * Przechowuje informacje o transakcji, takie jak identyfikator, typ,
     * kwota, data, klient i opis.
     * Implementuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class Transaction {
        private final SimpleIntegerProperty id;
        private final SimpleIntegerProperty clientId;
        private final SimpleStringProperty clientName;
        private final SimpleIntegerProperty productId;
        private final SimpleStringProperty productName;
        private final SimpleObjectProperty<BigDecimal> amount;
        private final SimpleObjectProperty<Timestamp> transactionDate;

        /**
         * Tworzy nową transakcję.
         *
         * @param id Identyfikator transakcji
         * @param clientId Identyfikator klienta
         * @param clientName Nazwa klienta
         * @param productId Identyfikator produktu
         * @param productName Nazwa produktu
         * @param amount Kwota transakcji
         * @param transactionDate Data transakcji
         */
        public Transaction(int id, int clientId, String clientName, int productId, String productName, BigDecimal amount, Timestamp transactionDate) {
            this.id = new SimpleIntegerProperty(id);
            this.clientId = new SimpleIntegerProperty(clientId);
            this.clientName = new SimpleStringProperty(clientName);
            this.productId = new SimpleIntegerProperty(productId);
            this.productName = new SimpleStringProperty(productName);
            this.amount = new SimpleObjectProperty<>(amount);
            this.transactionDate = new SimpleObjectProperty<>(transactionDate);
        }

        /**
         * Zwraca identyfikator transakcji.
         * @return Identyfikator transakcji
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca właściwość identyfikatora transakcji.
         * @return Właściwość identyfikatora
         */
        public SimpleIntegerProperty idProperty() { return id; }

        /**
         * Zwraca identyfikator klienta.
         * @return Identyfikator klienta
         */
        public int getClientId() { return clientId.get(); }

        /**
         * Zwraca właściwość identyfikatora klienta.
         * @return Właściwość identyfikatora klienta
         */
        public SimpleIntegerProperty clientIdProperty() { return clientId; }

        /**
         * Zwraca nazwę klienta.
         * @return Nazwa klienta
         */
        public String getClientName() { return clientName.get(); }

        /**
         * Zwraca właściwość nazwy klienta.
         * @return Właściwość nazwy klienta
         */
        public SimpleStringProperty clientNameProperty() { return clientName; }

        /**
         * Zwraca identyfikator produktu.
         * @return Identyfikator produktu
         */
        public int getProductId() { return productId.get(); }

        /**
         * Zwraca właściwość identyfikatora produktu.
         * @return Właściwość identyfikatora produktu
         */
        public SimpleIntegerProperty productIdProperty() { return productId; }

        /**
         * Zwraca nazwę produktu.
         * @return Nazwa produktu
         */
        public String getProductName() { return productName.get(); }

        /**
         * Zwraca właściwość nazwy produktu.
         * @return Właściwość nazwy produktu
         */
        public SimpleStringProperty productNameProperty() { return productName; }

        /**
         * Zwraca kwotę transakcji.
         * @return Kwota transakcji
         */
        public BigDecimal getAmount() { return amount.get(); }

        /**
         * Zwraca właściwość kwoty transakcji.
         * @return Właściwość kwoty
         */
        public SimpleObjectProperty<BigDecimal> amountProperty() { return amount; }

        /**
         * Zwraca datę transakcji.
         * @return Data transakcji
         */
        public Timestamp getTransactionDate() { return transactionDate.get(); }

        /**
         * Zwraca właściwość daty transakcji.
         * @return Właściwość daty
         */
        public SimpleObjectProperty<Timestamp> transactionDateProperty() { return transactionDate; }
    }

    /**
     * Klasa reprezentująca produkt w systemie.
     * Zawiera dane o nazwie, cenie i stanie magazynowym.
     */
    /**
     * Klasa reprezentująca produkt dostępny w siłowni.
     * Przechowuje informacje o produkcie, takie jak identyfikator, nazwa,
     * kategoria, cena, stan magazynowy i opis.
     * Wykorzystuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class Product {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty price;
        private final SimpleIntegerProperty stock;

        /**
         * Tworzy nowy produkt.
         *
         * @param id Identyfikator produktu
         * @param name Nazwa produktu
         * @param price Cena produktu
         * @param stock Stan magazynowy
         */
        public Product(int id, String name, double price, int stock) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.stock = new SimpleIntegerProperty(stock);
        }

        /**
         * Zwraca identyfikator produktu.
         * @return Identyfikator produktu
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca właściwość identyfikatora produktu.
         * @return Właściwość identyfikatora
         */
        public SimpleIntegerProperty idProperty() { return id; }

        /**
         * Zwraca nazwę produktu.
         * @return Nazwa produktu
         */
        public String getName() { return name.get(); }

        /**
         * Zwraca właściwość nazwy produktu.
         * @return Właściwość nazwy
         */
        public SimpleStringProperty nameProperty() { return name; }

        /**
         * Zwraca cenę produktu.
         * @return Cena produktu
         */
        public double getPrice() { return price.get(); }

        /**
         * Zwraca właściwość ceny produktu.
         * @return Właściwość ceny
         */
        public SimpleDoubleProperty priceProperty() { return price; }

        /**
         * Zwraca stan magazynowy produktu.
         * @return Stan magazynowy
         */
        public int getStock() { return stock.get(); }

        /**
         * Zwraca właściwość stanu magazynowego.
         * @return Właściwość stanu magazynowego
         */
        public SimpleIntegerProperty stockProperty() { return stock; }

        /**
         * Zwraca tekstową reprezentację produktu.
         * @return Nazwa produktu wraz z informacją o stanie magazynowym
         */
        @Override
        public String toString() {
            return name.get() + " (sztuk: " + stock.get() + ")";
        }
    }

    /**
     * Klasa reprezentująca płatność za karnet członkowski.
     * Zawiera dane o kliencie, kwocie i dacie płatności.
     */
    /**
     * Klasa reprezentująca płatność za karnet członkowski.
     * Przechowuje informacje o płatności, takie jak identyfikator, klient,
     * typ karnetu, data płatności, kwota i status.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class MembershipPayment {
        private final SimpleIntegerProperty id;
        private final SimpleIntegerProperty clientId; // Dodane, aby mieć dostęp do ID klienta
        private final SimpleStringProperty clientName;
        private final SimpleObjectProperty<BigDecimal> amount;
        private final SimpleObjectProperty<Timestamp> paymentDate;

        /**
         * Tworzy nową płatność za karnet.
         *
         * @param id Identyfikator płatności
         * @param clientId Identyfikator klienta
         * @param clientName Nazwa klienta
         * @param amount Kwota płatności
         * @param paymentDate Data płatności
         */
        public MembershipPayment(int id, int clientId, String clientName, BigDecimal amount, Timestamp paymentDate) {
            this.id = new SimpleIntegerProperty(id);
            this.clientId = new SimpleIntegerProperty(clientId);
            this.clientName = new SimpleStringProperty(clientName);
            this.amount = new SimpleObjectProperty<>(amount);
            this.paymentDate = new SimpleObjectProperty<>(paymentDate);
        }

        /**
         * Zwraca identyfikator płatności za trening personalny.
         * Identyfikator jest unikalny w systemie i pozwala na jednoznaczną
         * identyfikację płatności w bazie danych.
         *
         * @return Identyfikator płatności jako wartość typu int
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca właściwość identyfikatora płatności za trening personalny.
         * Metoda jest przydatna do wiązania danych w interfejsie JavaFX,
         * umożliwiając automatyczną aktualizację UI przy zmianie identyfikatora.
         *
         * @return Właściwość identyfikatora jako obiekt IntegerProperty
         */
        public SimpleIntegerProperty idProperty() { return id; }

        /**
         * Zwraca identyfikator klienta.
         * @return Identyfikator klienta
         */
        public int getClientId() { return clientId.get(); }

        /**
         * Zwraca właściwość identyfikatora klienta.
         * @return Właściwość identyfikatora klienta
         */
        public SimpleIntegerProperty clientIdProperty() { return clientId; }

        /**
         * Zwraca nazwę klienta.
         * @return Nazwa klienta
         */
        public String getClientName() { return clientName.get(); }

        /**
         * Zwraca właściwość nazwy klienta.
         * @return Właściwość nazwy klienta
         */
        public SimpleStringProperty clientNameProperty() { return clientName; }

        /**
         * Zwraca kwotę płatności.
         * @return Kwota płatności
         */
        public BigDecimal getAmount() { return amount.get(); }

        /**
         * Zwraca właściwość kwoty płatności.
         * @return Właściwość kwoty
         */
        public SimpleObjectProperty<BigDecimal> amountProperty() { return amount; }

        /**
         * Zwraca datę płatności.
         * @return Data płatności
         */
        public Timestamp getPaymentDate() { return paymentDate.get(); }

        /**
         * Zwraca właściwość daty płatności.
         * @return Właściwość daty
         */
        public SimpleObjectProperty<Timestamp> paymentDateProperty() { return paymentDate; }
    }

    /**
     * Klasa reprezentująca użytkownika systemu.
     * Zawiera dane o nazwie, emailu i roli.
     */
    /**
     * Klasa reprezentująca użytkownika systemu siłowni.
     * Przechowuje informacje o użytkowniku, takie jak identyfikator, imię i nazwisko,
     * email, hasło, rola i data rejestracji.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class User {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty email;
        private final SimpleStringProperty role;
        private final SimpleBooleanProperty isActive;

        /**
         * Tworzy nowego użytkownika.
         *
         * @param id Identyfikator użytkownika
         * @param name Imię i nazwisko
         * @param email Adres email
         * @param role Rola (client, trainer, employee)
         */
        public User(int id, String name, String email, String role) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.role = new SimpleStringProperty(role);
            this.isActive = new SimpleBooleanProperty(true);
        }

        public User(int id, String name, String email, String role, boolean isActive) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.role = new SimpleStringProperty(role);
            this.isActive = new SimpleBooleanProperty(isActive);
        }

        /**
         * Zwraca identyfikator użytkownika.
         * @return Identyfikator użytkownika
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca właściwość identyfikatora użytkownika.
         * @return Właściwość identyfikatora
         */
        public SimpleIntegerProperty idProperty() { return id; }

        /**
         * Zwraca imię i nazwisko użytkownika.
         * @return Imię i nazwisko
         */
        public String getName() { return name.get(); }

        /**
         * Zwraca właściwość imienia i nazwiska.
         * @return Właściwość imienia i nazwiska
         */
        public SimpleStringProperty nameProperty() { return name; }

        /**
         * Zwraca adres email użytkownika.
         * @return Adres email
         */
        public String getEmail() { return email.get(); }

        /**
         * Zwraca właściwość adresu email.
         * @return Właściwość adresu email
         */
        public SimpleStringProperty emailProperty() { return email; }

        /**
         * Zwraca rolę użytkownika.
         * @return Rola użytkownika
         */
        public String getRole() { return role.get(); }

        /**
         * Zwraca właściwość roli użytkownika.
         * @return Właściwość roli
         */
        public SimpleStringProperty roleProperty() { return role; }

        public boolean getIsActive() { return isActive.get(); }
        public void setIsActive(boolean value) { isActive.set(value); }
        public SimpleBooleanProperty isActiveProperty() { return isActive; }

        /**
         * Zwraca tekstową reprezentację użytkownika.
         * @return Imię i nazwisko użytkownika
         */
        @Override
        public String toString() {
            return name.get();
        }
    }

    /**
     * Klasa reprezentująca harmonogram zajęć.
     * Zawiera dane o dacie, godzinie, aktywności, trenerze, liczbie uczestników i statusie.
     *
     * @deprecated Ta klasa jest używana w starszym systemie harmonogramu zajęć grupowych.
     * Zalecane jest używanie klasy {@link TrainingRequestEntry} do obsługi harmonogramu treningów.
     */
    /**
     * Klasa reprezentująca harmonogram zajęć w siłowni.
     * @deprecated Ta klasa jest używana w starszym systemie harmonogramu zajęć grupowych.
     * Została zastąpiona nowszym rozwiązaniem.
     *
     * Przechowuje informacje o harmonogramie, takie jak identyfikator, nazwa zajęć,
     * dzień tygodnia, godzina rozpoczęcia, czas trwania i trener prowadzący.
     */
    public static class Schedule {
        private final SimpleStringProperty date;
        private final SimpleStringProperty time;
        private final SimpleStringProperty activity;
        private final SimpleStringProperty trainer;
        private final SimpleIntegerProperty participants;
        private final SimpleStringProperty status;

        /**
         * Tworzy nowy wpis w harmonogramie.
         *
         * @param date Data zajęć
         * @param time Godzina zajęć
         * @param activity Nazwa aktywności
         * @param trainer Imię i nazwisko trenera
         * @param participants Liczba uczestników
         * @param status Status zajęć
         */
        public Schedule(String date, String time, String activity, String trainer, int participants, String status) {
            this.date = new SimpleStringProperty(date);
            this.time = new SimpleStringProperty(time);
            this.activity = new SimpleStringProperty(activity);
            this.trainer = new SimpleStringProperty(trainer);
            this.participants = new SimpleIntegerProperty(participants);
            this.status = new SimpleStringProperty(status);
        }

        /**
         * Zwraca datę zajęć.
         * @return Data zajęć
         */
        public String getDate() { return date.get(); }

        /**
         * Zwraca właściwość daty zajęć.
         * @return Właściwość daty
         */
        public SimpleStringProperty dateProperty() { return date; }

        /**
         * Zwraca godzinę zajęć.
         * @return Godzina zajęć
         */
        public String getTime() { return time.get(); }

        /**
         * Zwraca właściwość godziny zajęć.
         * @return Właściwość godziny
         */
        public SimpleStringProperty timeProperty() { return time; }

        /**
         * Zwraca nazwę aktywności.
         * @return Nazwa aktywności
         */
        public String getActivity() { return activity.get(); }

        /**
         * Zwraca właściwość nazwy aktywności.
         * @return Właściwość nazwy aktywności
         */
        public SimpleStringProperty activityProperty() { return activity; }

        /**
         * Zwraca imię i nazwisko trenera.
         * @return Imię i nazwisko trenera
         */
        public String getTrainer() { return trainer.get(); }

        /**
         * Zwraca właściwość imienia i nazwiska trenera.
         * @return Właściwość imienia i nazwiska trenera
         */
        public SimpleStringProperty trainerProperty() { return trainer; }

        /**
         * Zwraca liczbę uczestników.
         * @return Liczba uczestników
         */
        public int getParticipants() { return participants.get(); }

        /**
         * Zwraca właściwość liczby uczestników.
         * @return Właściwość liczby uczestnik��w
         */
        public SimpleIntegerProperty participantsProperty() { return participants; }

        /**
         * Zwraca status zajęć.
         * @return Status zajęć
         */
        public String getStatus() { return status.get(); }

        /**
         * Zwraca właściwość statusu zajęć.
         * @return Właściwość statusu
         */
        public SimpleStringProperty statusProperty() { return status; }
    }




    /**
     * Klasa reprezentująca żądanie treningu personalnego.
     * Zawiera dane o kliencie, trenerze, dacie treningu, notatkach i statusie.
     */
    /**
     * Klasa reprezentująca żądanie treningu personalnego.
     * Przechowuje informacje o żądaniu, takie jak identyfikator, klient,
     * trener, data żądania, opis, status i płatności.
     * Implementuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class TrainingRequestEntry {
        private final SimpleIntegerProperty requestId;
        private final SimpleStringProperty clientName;
        private final SimpleStringProperty trainerName;
        private final SimpleObjectProperty<LocalDateTime> trainingDateTime;
        private final SimpleStringProperty requestNotes;
        private final SimpleStringProperty status;

        /**
         * Tworzy nowe żądanie treningu.
         *
         * @param requestId Identyfikator żądania
         * @param clientName Imię i nazwisko klienta
         * @param trainerName Imię i nazwisko trenera
         * @param trainingDateTime Data i godzina treningu (może być null dla oczekujących)
         * @param requestNotes Notatki dotyczące treningu
         * @param status Status żądania (np. "Zaplanowany", "Oczekujący")
         */
        public TrainingRequestEntry(int requestId, String clientName, String trainerName, LocalDateTime trainingDateTime, String requestNotes, String status) {
            this.requestId = new SimpleIntegerProperty(requestId);
            this.clientName = new SimpleStringProperty(clientName);
            this.trainerName = new SimpleStringProperty(trainerName);
            this.trainingDateTime = new SimpleObjectProperty<>(trainingDateTime);
            this.requestNotes = new SimpleStringProperty(requestNotes);
            this.status = new SimpleStringProperty(status);
        }

        /**
         * Zwraca identyfikator żądania.
         * @return Identyfikator żądania
         */
        public int getRequestId() { return requestId.get(); }

        /**
         * Zwraca właściwość identyfikatora żądania.
         * @return Właściwość identyfikatora
         */
        public SimpleIntegerProperty requestIdProperty() { return requestId; }

        /**
         * Zwraca imię i nazwisko klienta.
         * @return Imię i nazwisko klienta
         */
        public String getClientName() { return clientName.get(); }

        /**
         * Zwraca właściwość imienia i nazwiska klienta.
         * @return Właściwość imienia i nazwiska
         */
        public SimpleStringProperty clientNameProperty() { return clientName; }

        /**
         * Zwraca imię i nazwisko trenera.
         * @return Imię i nazwisko trenera
         */
        public String getTrainerName() { return trainerName.get(); }

        /**
         * Zwraca właściwość imienia i nazwiska trenera.
         * @return Właściwość imienia i nazwiska
         */
        public SimpleStringProperty trainerNameProperty() { return trainerName; }

        /**
         * Zwraca datę i godzinę treningu.
         * @return Data i godzina treningu lub null dla oczekujących
         */
        public LocalDateTime getTrainingDateTime() { return trainingDateTime.get(); }

        /**
         * Zwraca właściwość daty i godziny treningu.
         * @return Właściwość daty i godziny
         */
        public SimpleObjectProperty<LocalDateTime> trainingDateTimeProperty() { return trainingDateTime; }

        /**
         * Zwraca notatki dotyczące treningu.
         * @return Notatki
         */
        public String getRequestNotes() { return requestNotes.get(); }

        /**
         * Zwraca właściwość notatek.
         * @return Właściwość notatek
         */
        public SimpleStringProperty requestNotesProperty() { return requestNotes; }

        /**
         * Zwraca status żądania.
         * @return Status żądania
         */
        public String getStatus() { return status.get(); }

        /**
         * Zwraca właściwość statusu.
         * @return Właściwość statusu
         */
        public SimpleStringProperty statusProperty() { return status; }
    }

    /**
     * Klasa reprezentująca płatność za trening personalny.
     * Zawiera dane o identyfikatorze, dacie płatności i kwocie.
     */
    /**
     * Klasa reprezentująca płatność za trening personalny.
     * Przechowuje informacje o płatności, takie jak identyfikator,
     * data płatności i kwota.
     * Wykorzystuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class TrainingRequestPayment {
        private final SimpleIntegerProperty id;
        private final SimpleObjectProperty<Timestamp> paymentDate;
        private final SimpleObjectProperty<BigDecimal> amount;

        /**
         * Tworzy nową płatność za trening.
         *
         * @param id Identyfikator płatności
         * @param paymentDate Data płatności
         * @param amount Kwota płatności
         */
        public TrainingRequestPayment(int id, Timestamp paymentDate, BigDecimal amount) {
            this.id = new SimpleIntegerProperty(id);
            this.paymentDate = new SimpleObjectProperty<>(paymentDate);
            this.amount = new SimpleObjectProperty<>(amount);
        }

        /**
         * Zwraca identyfikator płatności.
         * @return Identyfikator płatności
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca właściwość identyfikatora płatności.
         * @return Właściwość identyfikatora
         */
        public SimpleIntegerProperty idProperty() { return id; }

        /**
         * Zwraca datę płatności za trening personalny.
         * Data płatności jest zapisywana jako obiekt Timestamp,
         * co pozwala na precyzyjne określenie zarówno daty jak i godziny
         * dokonania płatności. Informacja ta jest istotna dla
         * raportów finansowych i śledzenia historii płatności klientów.
         *
         * @return Data płatności jako obiekt Timestamp
         */
        public Timestamp getPaymentDate() { return paymentDate.get(); }

        /**
         * Zwraca właściwość daty płatności.
         * @return Właściwość daty
         */
        public SimpleObjectProperty<Timestamp> paymentDateProperty() { return paymentDate; }

        /**
         * Zwraca kwotę płatności za trening personalny.
         * Kwota jest przechowywana jako obiekt BigDecimal, co zapewnia
         * dokładność obliczeń finansowych i eliminuje błędy zaokrąglania.
         * Jest to istotne dla prawidłowego rozliczania płatności i generowania
         * raportów finansowych.
         *
         * @return Kwota płatności jako obiekt BigDecimal
         */
        public BigDecimal getAmount() { return amount.get(); }

        /**
         * Zwraca właściwość kwoty płatności.
         * @return Właściwość kwoty
         */
        public SimpleObjectProperty<BigDecimal> amountProperty() { return amount; }

    }


}

