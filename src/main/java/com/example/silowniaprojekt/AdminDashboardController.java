package com.example.silowniaprojekt;

import com.itextpdf.layout.properties.TextAlignment;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


public class AdminDashboardController {

    // Elementy UI - główne sekcje
    @FXML private VBox dashboardSection;
    @FXML private VBox usersSection;
    @FXML private VBox trainingsSection;
    @FXML private VBox storeSection;
    @FXML private VBox settingsSection;
    @FXML private VBox equipmentSection;
    @FXML private VBox reportsSection;

    // Elementy nawigacji
    @FXML private Button dashboardBtn;
    @FXML private Button usersBtn;
    @FXML private Button trainingsBtn;
    @FXML private Button storeBtn;
    @FXML private Button settingsBtn;
    @FXML private Button equipmentBtn;
    @FXML private Button reportsBtn;

    @FXML
    private ComboBox<String> productsPeriodComboBox;

    // Dashboard elements
    @FXML private LineChart<String, Number> activityChart;

    // Users section
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userStatusColumn;

    // Trainings section
    @FXML private TableView<Training> trainingsTable;
    @FXML private TableColumn<Training, String> trainingDateColumn;
    @FXML private TableColumn<Training, String> trainingNameColumn;
    @FXML private TableColumn<Training, String> trainingTrainerColumn;
    @FXML private TableColumn<Training, Integer> trainingParticipantsColumn;
    @FXML private TextField trainingNameField;
    @FXML private DatePicker trainingDatePicker;
    @FXML private ComboBox<String> trainerComboBox;

    // Store section
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, Double> productPriceColumn;
    @FXML private TableColumn<Product, Integer> productStockColumn;

    // Settings section
    @FXML private ComboBox<String> currencyComboBox;
    @FXML private CheckBox emailNotificationsCheckbox;
    @FXML private TextField basicPriceField;
    @FXML private TextField premiumPriceField;

    // Equipment section
    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, String> equipmentNameColumn;
    @FXML private TableColumn<Equipment, Integer> equipmentQuantityColumn;
    @FXML private TableColumn<Equipment, String> equipmentStatusColumn;

    // Reports section
    @FXML private ComboBox<String> financialPeriodComboBox;
    @FXML private ComboBox<String> activityPeriodComboBox;
    @FXML
    private ComboBox<String> membershipPaymentsPeriodComboBox;
    @FXML
    private ComboBox<String> transactionsPeriodComboBox;


    // Modele danych
    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<Training> trainings = FXCollections.observableArrayList();
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<Equipment> equipment = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Inicjalizacja wykresu
        initChart();

        // Inicjalizacja tabel
        initUserTable();
        initTrainingTable();
        initProductTable();
        initEquipmentTable();

        // Inicjalizacja danych
        loadDataFromDatabase();

        // Domyślna sekcja
        showDashboard();
        membershipPaymentsPeriodComboBox.getItems().addAll("Ostatni miesiąc", "Ostatni kwartał", "Wszystkie");
        membershipPaymentsPeriodComboBox.setValue("Wszystkie");
        transactionsPeriodComboBox.getItems().addAll("Ostatni tydzień", "Ostatni miesiąc", "Wszystkie");
        transactionsPeriodComboBox.setValue("Wszystkie");
    }

    @FXML
    private void generateTransactionsReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz raport transakcji");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(transactionsPeriodComboBox.getScene().getWindow());

            if (file != null) {
                ObservableList<TransactionReport> transactions = loadTransactionsFromDatabase();

                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Nagłówek
                Paragraph header = new Paragraph("Historia Transakcji\n\n")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(16);
                document.add(header);

                // Data i filtr
                Paragraph details = new Paragraph()
                        .add("Okres: " + transactionsPeriodComboBox.getValue() + "\n")
                        .add("Wygenerowano: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(10);
                document.add(details);

                // Tabela
                float[] columnWidths = {1, 2, 3, 3, 3, 2, 2};
                Table table = new Table(columnWidths);

                table.addHeaderCell("ID Transakcji");
                table.addHeaderCell("Data");
                table.addHeaderCell("Pracownik");
                table.addHeaderCell("Klient");
                table.addHeaderCell("Produkt");
                table.addHeaderCell("Kwota");
                table.addHeaderCell("Status");

                DecimalFormat df = new DecimalFormat("#,##0.00 zł");
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

                for (TransactionReport transaction : transactions) {
                    table.addCell(String.valueOf(transaction.getId()));
                    table.addCell(transaction.getTransactionDate().format(dateFormatter));
                    table.addCell(transaction.getEmployeeName());
                    table.addCell(transaction.getClientName());
                    table.addCell(transaction.getProductName());
                    table.addCell(df.format(transaction.getAmount()));
                    table.addCell(transaction.getPaymentStatus());
                }

                document.add(table);
                document.close();

                showAlert("Sukces", "Raport transakcji został wygenerowany: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert("Błąd", "Błąd generowania raportu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ObservableList<TransactionReport> loadTransactionsFromDatabase() {
        ObservableList<TransactionReport> transactions = FXCollections.observableArrayList();

        String query = "SELECT t.id, t.transaction_date, " +
                "e.name AS employee_name, c.name AS client_name, " +
                "p.name AS product_name, t.amount, t.payment_status " +
                "FROM transactions t " +
                "JOIN users e ON t.employee_id = e.id " +
                "JOIN users c ON t.client_id = c.id " +
                "JOIN products p ON t.product_id = p.id " +
                "WHERE 1=1";

        String period = transactionsPeriodComboBox.getValue();
        if (period != null) {
            switch (period) {
                case "Ostatni tydzień":
                    query += " AND t.transaction_date >= NOW() - INTERVAL 1 WEEK";
                    break;
                case "Ostatni miesiąc":
                    query += " AND t.transaction_date >= NOW() - INTERVAL 1 MONTH";
                    break;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                transactions.add(new TransactionReport(
                        rs.getInt("id"),
                        rs.getTimestamp("transaction_date").toLocalDateTime(),
                        rs.getString("employee_name"),
                        rs.getString("client_name"),
                        rs.getString("product_name"),
                        rs.getDouble("amount"),
                        rs.getString("payment_status")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd bazy danych: " + e.getMessage());
        }
        return transactions;
    }

    // Klasa modelu
    public static class TransactionReport {
        private final SimpleIntegerProperty id;
        private final LocalDateTime transactionDate;
        private final SimpleStringProperty employeeName;
        private final SimpleStringProperty clientName;
        private final SimpleStringProperty productName;
        private final SimpleDoubleProperty amount;
        private final SimpleStringProperty paymentStatus;

        public TransactionReport(int id, LocalDateTime transactionDate, String employeeName,
                                 String clientName, String productName, double amount,
                                 String paymentStatus) {
            this.id = new SimpleIntegerProperty(id);
            this.transactionDate = transactionDate;
            this.employeeName = new SimpleStringProperty(employeeName);
            this.clientName = new SimpleStringProperty(clientName);
            this.productName = new SimpleStringProperty(productName);
            this.amount = new SimpleDoubleProperty(amount);
            this.paymentStatus = new SimpleStringProperty(paymentStatus);
        }

        // Gettery
        public int getId() { return id.get(); }
        public LocalDateTime getTransactionDate() { return transactionDate; }
        public String getEmployeeName() { return employeeName.get(); }
        public String getClientName() { return clientName.get(); }
        public String getProductName() { return productName.get(); }
        public double getAmount() { return amount.get(); }
        public String getPaymentStatus() { return paymentStatus.get(); }
    }

    @FXML
    private void generateMembershipPaymentsReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz raport płatności");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(membershipPaymentsPeriodComboBox.getScene().getWindow());

            if (file != null) {
                ObservableList<MembershipPayment> payments = loadMembershipPaymentsFromDatabase();

                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Nagłówek
                Paragraph header = new Paragraph("Raport Płatności Członkowskich\n\n")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(16);
                document.add(header);

                // Data i okres
                Paragraph details = new Paragraph()
                        .add("Okres: " + membershipPaymentsPeriodComboBox.getValue() + "\n")
                        .add("Wygenerowano: " + LocalDateTime.now() + "\n\n")
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(10);
                document.add(details);

                // Tabela
                float[] columnWidths = {1, 3, 2, 2, 2};
                Table table = new Table(columnWidths);
                table.addHeaderCell("ID Płatności");
                table.addHeaderCell("Klient");
                table.addHeaderCell("Kwota");
                table.addHeaderCell("Data płatności");
                table.addHeaderCell("Status");

                DecimalFormat df = new DecimalFormat("#,##0.00 zł");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

                for (MembershipPayment payment : payments) {
                    table.addCell(String.valueOf(payment.getId()));
                    table.addCell(payment.getClientName());
                    table.addCell(df.format(payment.getAmount()));
                    table.addCell(payment.getPaymentDate().format(dtf));
                    table.addCell(payment.getStatus());
                }

                document.add(table);
                document.close();

                showAlert("Sukces", "Raport płatności został wygenerowany: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert("Błąd", "Nie udało się wygenerować raportu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ObservableList<MembershipPayment> loadMembershipPaymentsFromDatabase() {
        ObservableList<MembershipPayment> payments = FXCollections.observableArrayList();

        String query = "SELECT mp.*, u.name AS client_name " +
                "FROM membership_payments mp " +
                "JOIN users u ON mp.client_id = u.id " +
                "WHERE 1=1 ";

        String period = membershipPaymentsPeriodComboBox.getValue();
        if (period != null) {
            switch (period) {
                case "Ostatni miesiąc":
                    query += " AND mp.payment_date >= NOW() - INTERVAL 1 MONTH";
                    break;
                case "Ostatni kwartał":
                    query += " AND mp.payment_date >= NOW() - INTERVAL 3 MONTH";
                    break;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                payments.add(new MembershipPayment(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("client_name"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("payment_date").toLocalDateTime(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Nie udało się pobrać danych: " + e.getMessage());
        }
        return payments;
    }

    // Klasa modelu
    public static class MembershipPayment {
        private final SimpleIntegerProperty id;
        private final SimpleIntegerProperty clientId;
        private final SimpleStringProperty clientName;
        private final SimpleDoubleProperty amount;
        private final LocalDateTime paymentDate;
        private final SimpleStringProperty status;

        public MembershipPayment(int id, int clientId, String clientName, double amount,
                                 LocalDateTime paymentDate, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.clientId = new SimpleIntegerProperty(clientId);
            this.clientName = new SimpleStringProperty(clientName);
            this.amount = new SimpleDoubleProperty(amount);
            this.paymentDate = paymentDate;
            this.status = new SimpleStringProperty(status);
        }

        // Gettery
        public int getId() { return id.get(); }
        public int getClientId() { return clientId.get(); }
        public String getClientName() { return clientName.get(); }
        public double getAmount() { return amount.get(); }
        public LocalDateTime getPaymentDate() { return paymentDate; }
        public String getStatus() { return status.get(); }
    }
    @FXML
    private void generateProductsReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz raport produktów");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            java.io.File file = fileChooser.showSaveDialog(productsPeriodComboBox.getScene().getWindow());

            if (file != null) {
                // Pobierz dane z bazy
                ObservableList<Product> products = loadProductsFromDatabase();

                // Stwórz dokument PDF
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Nagłówek
                Paragraph header = new Paragraph("Raport Produktów\n\n")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(16);
                document.add(header);

                // Data wygenerowania
                Paragraph date = new Paragraph("Wygenerowano: " + java.time.LocalDateTime.now() + "\n\n")
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(10);
                document.add(date);

                // Tabela z danymi
                float[] columnWidths = {1, 3, 2, 2};
                Table table = new Table(columnWidths);
                table.addHeaderCell("ID");
                table.addHeaderCell("Nazwa produktu");
                table.addHeaderCell("Cena");
                table.addHeaderCell("Stan magazynu");

                for (Product product : products) {
                    table.addCell(String.valueOf(product.getId()));
                    table.addCell(product.getName());
                    table.addCell(String.format("%.2f zł", product.getPrice()));
                    table.addCell(String.valueOf(product.getStock()));
                }

                document.add(table);
                document.close();

                showAlert("Sukces", "Raport produktów został wygenerowany: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert("Błąd", "Nie udało się wygenerować raportu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ObservableList<Product> loadProductsFromDatabase() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM products";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Nie udało się pobrać danych: " + e.getMessage());
        }
        return products;
    }
    // Inicjalizacja wykresu
    private void initChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Aktywność klientów");

        series.getData().add(new XYChart.Data<>("Pon", 65));
        series.getData().add(new XYChart.Data<>("Wt", 59));
        series.getData().add(new XYChart.Data<>("Śr", 80));
        series.getData().add(new XYChart.Data<>("Czw", 81));
        series.getData().add(new XYChart.Data<>("Pt", 56));
        series.getData().add(new XYChart.Data<>("Sob", 55));
        series.getData().add(new XYChart.Data<>("Nd", 40));

        activityChart.getData().add(series);
    }

    // Inicjalizacja tabeli użytkowników
    private void initUserTable() {
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        usersTable.setItems(users);
    }

    // Inicjalizacja tabeli treningów
    private void initTrainingTable() {
        trainingDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        trainingNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        trainingTrainerColumn.setCellValueFactory(new PropertyValueFactory<>("trainer"));
        trainingParticipantsColumn.setCellValueFactory(new PropertyValueFactory<>("participants"));

        trainingsTable.setItems(trainings);

        // Inicjalizacja comboboxa trenerów
        trainerComboBox.getItems().addAll("Anna Nowak", "Jan Kowalski", "Piotr Zieliński");
        trainingDatePicker.setValue(LocalDate.now());
    }

    // Inicjalizacja tabeli produktów
    private void initProductTable() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        productsTable.setItems(products);
    }

    // Inicjalizacja tabeli sprzętu
    private void initEquipmentTable() {
        equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        equipmentQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        equipmentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        equipmentTable.setItems(equipment);
    }

    // Ładowanie danych z bazy
    private void loadDataFromDatabase() {
        // Wczytaj dane z bazy lub dodaj przykładowe
        loadUsers();
        loadTrainings();
        loadProducts();
        loadEquipment();
    }

    // Wczytaj użytkowników
    private void loadUsers() {
        users.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                String query = "SELECT id, name, email, role FROM users";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    users.add(new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            "Aktywny"
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania użytkowników: " + e.getMessage());
            // Dodaj przykładowe dane
            addSampleUsers();
        }

        // Jeśli nie ma danych, dodaj przykładowe
        if (users.isEmpty()) {
            addSampleUsers();
        }
    }

    private void addSampleUsers() {
        users.add(new User(1, "Jan Kowalski", "jan@example.com", "client", "Aktywny"));
        users.add(new User(2, "Anna Nowak", "anna@example.com", "trainer", "Aktywny"));
        users.add(new User(3, "Piotr Zieliński", "piotr@example.com", "employee", "Nieaktywny"));
        users.add(new User(4, "Admin", "admin@example.com", "admin", "Aktywny"));
    }

    // Wczytaj treningi
    private void loadTrainings() {
        trainings.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                String query = "SELECT gw.id, gw.session_type, gw.workout_date, u.name AS trainer_name, gw.max_participants " +
                        "FROM group_workouts gw JOIN users u ON gw.trainer_id = u.id";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    trainings.add(new Training(
                            rs.getInt("id"),
                            rs.getString("session_type"),
                            rs.getString("workout_date"),
                            rs.getString("trainer_name"),
                            rs.getInt("max_participants")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania treningów: " + e.getMessage());
            // Dodaj przykładowe dane
            addSampleTrainings();
        }

        // Jeśli nie ma danych, dodaj przykładowe
        if (trainings.isEmpty()) {
            addSampleTrainings();
        }
    }

    private void addSampleTrainings() {
        trainings.add(new Training(1, "CrossFit Advanced", "2024-04-30 18:00", "Anna Nowak", 12));
        trainings.add(new Training(2, "Yoga Basic", "2024-04-30 20:00", "Jan Kowalski", 15));
        trainings.add(new Training(3, "Trening siłowy", "2024-05-01 17:00", "Piotr Zieliński", 8));
    }

    // Wczytaj produkty
    private void loadProducts() {
        products.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                String query = "SELECT id, name, price, stock FROM products";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    products.add(new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania produktów: " + e.getMessage());
            // Dodaj przykładowe dane
            addSampleProducts();
        }

        // Jeśli nie ma danych, dodaj przykładowe
        if (products.isEmpty()) {
            addSampleProducts();
        }
    }

    private void addSampleProducts() {
        products.add(new Product(1, "Odżywka białkowa", 120.0, 45));
        products.add(new Product(2, "Napój energetyczny", 9.99, 100));
        products.add(new Product(3, "Mata do jogi", 29.99, 30));
    }

    // Wczytaj sprzęt
    private void loadEquipment() {
        equipment.clear();
        // Dodaj przykładowy sprzęt (brak tabeli w bazie)
        equipment.add(new Equipment(1, "Bieżnia elektryczna", 5, "Dostępny"));
        equipment.add(new Equipment(2, "Ławka do wyciskania", 3, "Dostępny"));
        equipment.add(new Equipment(3, "Sztangi (20kg)", 10, "Dostępny"));
    }

    // Metody obsługi nawigacji
    @FXML
    private void showDashboard() {
        hideAllSections();
        dashboardSection.setVisible(true);
        setActiveButton(dashboardBtn);
    }

    @FXML
    private void showUsers() {
        hideAllSections();
        usersSection.setVisible(true);
        setActiveButton(usersBtn);
    }

    @FXML
    private void showTrainings() {
        hideAllSections();
        trainingsSection.setVisible(true);
        setActiveButton(trainingsBtn);
    }

    @FXML
    private void showStore() {
        hideAllSections();
        storeSection.setVisible(true);
        setActiveButton(storeBtn);
    }

    @FXML
    private void showSettings() {
        hideAllSections();
        settingsSection.setVisible(true);
        setActiveButton(settingsBtn);
    }

    @FXML
    private void showEquipment() {
        hideAllSections();
        equipmentSection.setVisible(true);
        setActiveButton(equipmentBtn);
    }

    @FXML
    private void showReports() {
        hideAllSections();
        reportsSection.setVisible(true);
        setActiveButton(reportsBtn);
    }

    private void hideAllSections() {
        dashboardSection.setVisible(false);
        usersSection.setVisible(false);
        trainingsSection.setVisible(false);
        storeSection.setVisible(false);
        settingsSection.setVisible(false);
        equipmentSection.setVisible(false);
        reportsSection.setVisible(false);
    }

    private void setActiveButton(Button activeButton) {
        // Zresetuj style wszystkich przycisków
        dashboardBtn.setStyle("");
        usersBtn.setStyle("");
        trainingsBtn.setStyle("");
        storeBtn.setStyle("");
        settingsBtn.setStyle("");
        equipmentBtn.setStyle("");
        reportsBtn.setStyle("");

        // Ustaw styl aktywnego przycisku
        activeButton.setStyle("-fx-background-color: #dc143c;");
    }

    // Akcje użytkownika
    @FXML
    private void logout() {
        try {
            // Zamknij bieżące okno
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();

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

    @FXML
    private void addUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Dodaj nowego użytkownika");
        dialog.setHeaderText("Wprowadź dane nowego użytkownika");

        // Przyciski
        ButtonType addButton = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        // Pola formularza
        TextField nameField = new TextField();
        nameField.setPromptText("Imię i nazwisko");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Hasło");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("client", "trainer", "employee", "admin");
        roleBox.setValue("client");

        // Układ
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Imię i nazwisko:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Hasło:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Rola:"), 0, 3);
        grid.add(roleBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                return new User(0, nameField.getText(), emailField.getText(), roleBox.getValue(), "Aktywny");
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(user -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    String query = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, user.getName());
                    stmt.setString(2, user.getEmail());
                    stmt.setString(3, passwordField.getText()); // W rzeczywistej aplikacji należy zahaszować hasło
                    stmt.setString(4, user.getRole());

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next()) {
                            user.setId(rs.getInt(1));
                        }
                        users.add(user);
                        showAlert("Sukces", "Użytkownik został dodany.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Błąd podczas dodawania użytkownika: " + e.getMessage());
                // Dodaj użytkownika lokalnie w przypadku problemów z bazą
                user.setId(users.size() + 1);
                users.add(user);
                showAlert("Uwaga", "Użytkownik został dodany lokalnie, ale wystąpił problem z bazą danych.");
            }
        });
    }

    @FXML
    private void addTraining() {
        if (trainingNameField.getText().isEmpty() || trainingDatePicker.getValue() == null || trainerComboBox.getValue() == null) {
            showAlert("Błąd", "Wypełnij wszystkie pola");
            return;
        }

        String name = trainingNameField.getText();
        LocalDate date = trainingDatePicker.getValue();
        String trainer = trainerComboBox.getValue();

        Training training = new Training(
                trainings.size() + 1,
                name,
                date.toString() + " 18:00",
                trainer,
                10
        );

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                // Znajdź ID trenera
                int trainerId = 0;
                String trainerQuery = "SELECT id FROM users WHERE name = ? AND role = 'trainer'";
                PreparedStatement trainerStmt = conn.prepareStatement(trainerQuery);
                trainerStmt.setString(1, trainer);
                ResultSet trainerRs = trainerStmt.executeQuery();

                if (trainerRs.next()) {
                    trainerId = trainerRs.getInt("id");
                } else {
                    showAlert("Błąd", "Nie znaleziono trenera o podanym imieniu");
                    return;
                }

                // Dodaj trening do bazy
                String query = "INSERT INTO group_workouts (trainer_id, workout_date, session_type, max_participants) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, trainerId);
                stmt.setString(2, date.toString() + " 18:00:00");
                stmt.setString(3, name);
                stmt.setInt(4, 10);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        training.setId(rs.getInt(1));
                    }
                    trainings.add(training);
                    showAlert("Sukces", "Trening został dodany.");

                    // Wyczyść pola formularza
                    trainingNameField.clear();
                    trainingDatePicker.setValue(LocalDate.now());
                    trainerComboBox.getSelectionModel().clearSelection();
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania treningu: " + e.getMessage());
            // Dodaj trening lokalnie w przypadku problemów z bazą
            trainings.add(training);
            showAlert("Uwaga", "Trening został dodany lokalnie, ale wystąpił problem z bazą danych.");

            // Wyczyść pola formularza
            trainingNameField.clear();
            trainingDatePicker.setValue(LocalDate.now());
            trainerComboBox.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void addProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Dodaj nowy produkt");
        dialog.setHeaderText("Wprowadź dane nowego produktu");

        // Przyciski
        ButtonType addButton = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        // Pola formularza
        TextField nameField = new TextField();
        nameField.setPromptText("Nazwa produktu");

        TextField priceField = new TextField();
        priceField.setPromptText("Cena");

        TextField stockField = new TextField();
        stockField.setPromptText("Ilość w magazynie");

        // Układ
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nazwa:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Cena:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Ilość:"), 0, 2);
        grid.add(stockField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    double price = Double.parseDouble(priceField.getText().replace(",", "."));
                    int stock = Integer.parseInt(stockField.getText());
                    return new Product(0, nameField.getText(), price, stock);
                } catch (NumberFormatException e) {
                    showAlert("Błąd", "Nieprawidłowy format liczby");
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();

        result.ifPresent(product -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    String query = "INSERT INTO products (name, price, stock) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, product.getName());
                    stmt.setDouble(2, product.getPrice());
                    stmt.setInt(3, product.getStock());

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next()) {
                            product.setId(rs.getInt(1));
                        }
                        products.add(product);
                        showAlert("Sukces", "Produkt został dodany.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Błąd podczas dodawania produktu: " + e.getMessage());
                // Dodaj produkt lokalnie w przypadku problemów z bazą
                product.setId(products.size() + 1);
                products.add(product);
                showAlert("Uwaga", "Produkt został dodany lokalnie, ale wystąpił problem z bazą danych.");
            }
        });
    }

    @FXML
    private void addEquipment() {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle("Dodaj nowy sprzęt");
        dialog.setHeaderText("Wprowadź dane nowego sprzętu");

        // Przyciski
        ButtonType addButton = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        // Pola formularza
        TextField nameField = new TextField();
        nameField.setPromptText("Nazwa sprzętu");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Ilość");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Dostępny", "W naprawie", "Niedostępny");
        statusBox.setValue("Dostępny");

        // Układ
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nazwa:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Ilość:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    int quantity = Integer.parseInt(quantityField.getText());
                    return new Equipment(0, nameField.getText(), quantity, statusBox.getValue());
                } catch (NumberFormatException e) {
                    showAlert("Błąd", "Nieprawidłowy format liczby");
                    return null;
                }
            }
            return null;
        });

        Optional<Equipment> result = dialog.showAndWait();

        result.ifPresent(item -> {
            item.setId(equipment.size() + 1);
            equipment.add(item);
            showAlert("Sukces", "Sprzęt został dodany.");
        });
    }

    @FXML
    private void saveSettings() {
        try {
            double basicPrice = Double.parseDouble(basicPriceField.getText().replace(",", "."));
            double premiumPrice = Double.parseDouble(premiumPriceField.getText().replace(",", "."));

            // Tutaj można zapisać ustawienia do bazy danych
            showAlert("Sukces", "Ustawienia zostały zapisane.");
        } catch (NumberFormatException e) {
            showAlert("Błąd", "Nieprawidłowy format ceny");
        }
    }

    @FXML
    private void generateFinancialReport() {
        String period = financialPeriodComboBox.getValue();
        if (period == null) {
            showAlert("Błąd", "Wybierz okres raportu");
            return;
        }

        showAlert("Sukces", "Raport finansowy za " + period.toLowerCase() + " został wygenerowany.");
    }

    @FXML
    private void generateActivityReport() {
        String period = activityPeriodComboBox.getValue();
        if (period == null) {
            showAlert("Błąd", "Wybierz okres raportu");
            return;
        }

        showAlert("Sukces", "Raport aktywności za " + period.toLowerCase() + " został wygenerowany.");
    }

    // Metody pomocnicze
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Klasy modelu danych
    public static class User {
        private SimpleIntegerProperty id;
        private SimpleStringProperty name;
        private SimpleStringProperty email;
        private SimpleStringProperty role;
        private SimpleStringProperty status;

        public User(int id, String name, String email, String role, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.role = new SimpleStringProperty(role);
            this.status = new SimpleStringProperty(status);
        }

        // Gettery i settery
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }

        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }

        public String getEmail() { return email.get(); }
        public void setEmail(String email) { this.email.set(email); }

        public String getRole() { return role.get(); }
        public void setRole(String role) { this.role.set(role); }

        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
    }

    public static class Training {
        private SimpleIntegerProperty id;
        private SimpleStringProperty name;
        private SimpleStringProperty date;
        private SimpleStringProperty trainer;
        private SimpleIntegerProperty participants;

        public Training(int id, String name, String date, String trainer, int participants) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.date = new SimpleStringProperty(date);
            this.trainer = new SimpleStringProperty(trainer);
            this.participants = new SimpleIntegerProperty(participants);
        }

        // Gettery i settery
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }

        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }

        public String getDate() { return date.get(); }
        public void setDate(String date) { this.date.set(date); }

        public String getTrainer() { return trainer.get(); }
        public void setTrainer(String trainer) { this.trainer.set(trainer); }

        public int getParticipants() { return participants.get(); }
        public void setParticipants(int participants) { this.participants.set(participants); }
    }

    public static class Product {
        private SimpleIntegerProperty id;
        private SimpleStringProperty name;
        private SimpleDoubleProperty price;
        private SimpleIntegerProperty stock;

        public Product(int id, String name, double price, int stock) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.stock = new SimpleIntegerProperty(stock);
        }

        // Gettery i settery
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }

        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }

        public double getPrice() { return price.get(); }
        public void setPrice(double price) { this.price.set(price); }

        public int getStock() { return stock.get(); }
        public void setStock(int stock) { this.stock.set(stock); }
    }

    public static class Equipment {
        private SimpleIntegerProperty id;
        private SimpleStringProperty name;
        private SimpleIntegerProperty quantity;
        private SimpleStringProperty status;

        public Equipment(int id, String name, int quantity, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.status = new SimpleStringProperty(status);
        }

        // Gettery i settery
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }

        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }

        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int quantity) { this.quantity.set(quantity); }

        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
    }
}