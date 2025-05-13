package com.example.silowniaprojekt;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EmployeeDashboardController {

    // Sekcje
    @FXML private VBox dashboardSection;
    @FXML private VBox registrationSection;
    @FXML private VBox warehouseSection;
    @FXML private VBox pricingSection;
    @FXML private VBox transactionsSection;
    @FXML private VBox reportsSection;
    @FXML private VBox scheduleSection;

    // Rejestracja
    @FXML private TextField clientNameField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> membershipComboBox;

    // Magazyn
    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, String> eqNameColumn;
    @FXML private TableColumn<Equipment, Integer> eqQuantityColumn;
    @FXML private TableColumn<Equipment, String> eqStatusColumn;
    @FXML private TableColumn<Equipment, String> eqLastCheckColumn;
    @FXML private TableColumn<Equipment, Void> eqActionsColumn;
    @FXML private TextField equipmentNameField;
    @FXML private TextField equipmentQuantityField;
    private ObservableList<Equipment> equipmentData = FXCollections.observableArrayList();

    // Cennik
    @FXML private TableView<PricingPackage> pricingTable;
    @FXML private TableColumn<PricingPackage, String> membershipNameColumn;
    @FXML private TableColumn<PricingPackage, String> membershipDescColumn;
    @FXML private TableColumn<PricingPackage, Double> membershipPriceColumn;
    @FXML private TableColumn<PricingPackage, String> membershipDurationColumn;
    @FXML private TableColumn<PricingPackage, String> membershipStatusColumn;
    @FXML private TextField packageNameField;
    @FXML private TextField packagePriceField;
    @FXML private ComboBox<String> packageDurationCombo;
    private ObservableList<PricingPackage> pricingData = FXCollections.observableArrayList();

    // Transakcje
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> transactionDateColumn;
    @FXML private TableColumn<Transaction, String> transactionIdColumn;
    @FXML private TableColumn<Transaction, String> transactionClientColumn;
    @FXML private TableColumn<Transaction, Double> transactionAmountColumn;
    @FXML private TableColumn<Transaction, String> transactionTypeColumn;
    @FXML private TableColumn<Transaction, String> transactionStatusColumn;
    @FXML private ComboBox<String> clientComboBox;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> transactionTypeCombo;
    private ObservableList<Transaction> transactionData = FXCollections.observableArrayList();

    // Raporty
    @FXML private Label revenueLabel;
    @FXML private Label revenueChangeLabel;
    @FXML private Label newClientsLabel;
    @FXML private Label clientsChangeLabel;
    @FXML private Label attendanceLabel;
    @FXML private Label attendanceChangeLabel;

    // Harmonogram
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> scheduleDateColumn;
    @FXML private TableColumn<Schedule, String> scheduleTimeColumn;
    @FXML private TableColumn<Schedule, String> scheduleActivityColumn;
    @FXML private TableColumn<Schedule, String> scheduleTrainerColumn;
    @FXML private TableColumn<Schedule, Integer> scheduleParticipantsColumn;
    @FXML private TableColumn<Schedule, String> scheduleStatusColumn;
    @FXML private DatePicker scheduleDatePicker;
    @FXML private TextField activityNameField;
    @FXML private ComboBox<String> trainerComboBox;
    private ObservableList<Schedule> scheduleData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initializeRegistration();
        initializeWarehouse();
        initializePricing();
        initializeTransactions();
        initializeSchedule();
        initializeReports();
        hideAllSections();
        showDashboard();
    }

    private void initializeRegistration() {
        membershipComboBox.getItems().addAll(
                "Standardowy (100 zł/mc)",
                "Premium (200 zł/mc)",
                "VIP (500 zł/mc)"
        );
        birthDatePicker.setValue(LocalDate.now());
    }

    private void initializeWarehouse() {
        eqNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        eqQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        eqStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        eqLastCheckColumn.setCellValueFactory(cellData -> cellData.getValue().lastCheckProperty());

        // Inicjalizacja kolumny akcji
        eqActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Usuń");

            {
                deleteButton.setOnAction(event -> {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    equipmentData.remove(equipment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        equipmentTable.setItems(equipmentData);
        equipmentData.add(new Equipment("Hantle 5kg", 20, "Dostępne", LocalDate.now().toString()));
    }

    private void initializePricing() {
        packageDurationCombo.getItems().addAll("1 miesiąc", "3 miesiące", "6 miesięcy", "12 miesięcy");

        membershipNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        membershipDescColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        membershipPriceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        membershipDurationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
        membershipStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        pricingData.addAll(
                new PricingPackage("Standardowy", "Dostęp do podstawowej strefy", 100.0, "1 miesiąc", "Aktywny"),
                new PricingPackage("Premium", "Dostęp do wszystkich stref + sauna", 200.0, "1 miesiąc", "Aktywny")
        );
        pricingTable.setItems(pricingData);
    }

    private void initializeTransactions() {
        clientComboBox.getItems().addAll("Jan Kowalski", "Anna Nowak", "Piotr Wiśniewski");
        transactionTypeCombo.getItems().addAll("Karnet", "Sklep", "Usługa");

        transactionDateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        transactionIdColumn.setCellValueFactory(cellData -> cellData.getValue().transactionIdProperty());
        transactionClientColumn.setCellValueFactory(cellData -> cellData.getValue().clientProperty());
        transactionAmountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        transactionTypeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        transactionStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        transactionData.add(new Transaction(
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                "TX-001",
                "Jan Kowalski",
                200.0,
                "Karnet",
                "Zakończona"
        ));
        transactionsTable.setItems(transactionData);
    }

    private void initializeSchedule() {
        trainerComboBox.getItems().addAll("Anna Nowak", "Marek Kowalski", "Katarzyna Wiśniewska");

        scheduleDateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        scheduleTimeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        scheduleActivityColumn.setCellValueFactory(cellData -> cellData.getValue().activityProperty());
        scheduleTrainerColumn.setCellValueFactory(cellData -> cellData.getValue().trainerProperty());
        scheduleParticipantsColumn.setCellValueFactory(cellData -> cellData.getValue().participantsProperty().asObject());
        scheduleStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        scheduleData.add(new Schedule(
                "2023-08-15",
                "18:00",
                "Joga",
                "Anna Nowak",
                12,
                "Planowane"
        ));
        scheduleTable.setItems(scheduleData);
    }

    private void initializeReports() {
        revenueLabel.setText("24 500 zł");
        revenueChangeLabel.setText("▲ 15% (m/m)");
        newClientsLabel.setText("47");
        clientsChangeLabel.setText("▼ 5% (m/m)");
        attendanceLabel.setText("68%");
        attendanceChangeLabel.setText("▬ 0% (m/m)");
    }

    @FXML
    private void registerClient() {
        if (clientNameField.getText().isEmpty() || membershipComboBox.getValue() == null) {
            showError("Wypełnij wszystkie wymagane pola!");
            return;
        }

        // Logika rejestracji
        showError("Klient zarejestrowany pomyślnie!");
        clientNameField.clear();
        membershipComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void addEquipment() {
        try {
            Equipment newEquipment = new Equipment(
                    equipmentNameField.getText(),
                    Integer.parseInt(equipmentQuantityField.getText()),
                    "Dostępne",
                    LocalDate.now().toString()
            );
            equipmentData.add(newEquipment);
            equipmentNameField.clear();
            equipmentQuantityField.clear();
        } catch (NumberFormatException e) {
            showError("Nieprawidłowy format liczby!");
        }
    }

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
    private void addTransaction() {
        try {
            if (clientComboBox.getValue() == null || amountField.getText().isEmpty()) {
                showError("Wybierz klienta i podaj kwotę!");
                return;
            }

            Transaction newTransaction = new Transaction(
                    LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                    "TX-" + (transactionData.size() + 1),
                    clientComboBox.getValue(),
                    Double.parseDouble(amountField.getText()),
                    transactionTypeCombo.getValue(),
                    "Oczekująca"
            );
            transactionData.add(newTransaction);
            clearTransactionFields();
        } catch (NumberFormatException e) {
            showError("Nieprawidłowy format kwoty!");
        }
    }

    @FXML
    private void addSchedule() {
        if (scheduleDatePicker.getValue() == null || activityNameField.getText().isEmpty()) {
            showError("Wybierz datę i nazwę zajęć!");
            return;
        }

        Schedule newSchedule = new Schedule(
                scheduleDatePicker.getValue().format(DateTimeFormatter.ISO_DATE),
                "18:00",
                activityNameField.getText(),
                trainerComboBox.getValue(),
                0,
                "Planowane"
        );
        scheduleData.add(newSchedule);
        clearScheduleFields();
    }

    private void hideAllSections() {
        dashboardSection.setVisible(false);
        registrationSection.setVisible(false);
        warehouseSection.setVisible(false);
        pricingSection.setVisible(false);
        transactionsSection.setVisible(false);
        reportsSection.setVisible(false);
        scheduleSection.setVisible(false);
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
        clientComboBox.getSelectionModel().clearSelection();
        amountField.clear();
        transactionTypeCombo.getSelectionModel().clearSelection();
    }

    private void clearScheduleFields() {
        scheduleDatePicker.setValue(null);
        activityNameField.clear();
        trainerComboBox.getSelectionModel().clearSelection();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Metody przełączania sekcji
    @FXML private void showDashboard() { toggleSection(dashboardSection); }
    @FXML private void showRegistration() { toggleSection(registrationSection); }
    @FXML private void showWarehouse() { toggleSection(warehouseSection); }
    @FXML private void showPricing() { toggleSection(pricingSection); }
    @FXML private void showTransactions() { toggleSection(transactionsSection); }
    @FXML private void showReports() { toggleSection(reportsSection); }
    @FXML private void showSchedule() { toggleSection(scheduleSection); }

    // Klasy modelowe
    public static class Equipment {
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty quantity;
        private final SimpleStringProperty status;
        private final SimpleStringProperty lastCheck;

        public Equipment(String name, int quantity, String status, String lastCheck) {
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.status = new SimpleStringProperty(status);
            this.lastCheck = new SimpleStringProperty(lastCheck);
        }

        public String getName() { return name.get(); }
        public int getQuantity() { return quantity.get(); }
        public String getStatus() { return status.get(); }
        public String getLastCheck() { return lastCheck.get(); }

        public SimpleStringProperty nameProperty() { return name; }
        public SimpleIntegerProperty quantityProperty() { return quantity; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty lastCheckProperty() { return lastCheck; }
    }

    public static class PricingPackage {
        private final SimpleStringProperty name;
        private final SimpleStringProperty description;
        private final SimpleDoubleProperty price;
        private final SimpleStringProperty duration;
        private final SimpleStringProperty status;

        public PricingPackage(String name, String description, double price, String duration, String status) {
            this.name = new SimpleStringProperty(name);
            this.description = new SimpleStringProperty(description);
            this.price = new SimpleDoubleProperty(price);
            this.duration = new SimpleStringProperty(duration);
            this.status = new SimpleStringProperty(status);
        }

        public String getName() { return name.get(); }
        public String getDescription() { return description.get(); }
        public double getPrice() { return price.get(); }
        public String getDuration() { return duration.get(); }
        public String getStatus() { return status.get(); }

        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleDoubleProperty priceProperty() { return price; }
        public SimpleStringProperty durationProperty() { return duration; }
        public SimpleStringProperty statusProperty() { return status; }
    }

    public static class Transaction {
        private final SimpleStringProperty date;
        private final SimpleStringProperty transactionId;
        private final SimpleStringProperty client;
        private final SimpleDoubleProperty amount;
        private final SimpleStringProperty type;
        private final SimpleStringProperty status;

        public Transaction(String date, String transactionId, String client, double amount, String type, String status) {
            this.date = new SimpleStringProperty(date);
            this.transactionId = new SimpleStringProperty(transactionId);
            this.client = new SimpleStringProperty(client);
            this.amount = new SimpleDoubleProperty(amount);
            this.type = new SimpleStringProperty(type);
            this.status = new SimpleStringProperty(status);
        }

        public String getDate() { return date.get(); }
        public String getTransactionId() { return transactionId.get(); }
        public String getClient() { return client.get(); }
        public double getAmount() { return amount.get(); }
        public String getType() { return type.get(); }
        public String getStatus() { return status.get(); }

        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty transactionIdProperty() { return transactionId; }
        public SimpleStringProperty clientProperty() { return client; }
        public SimpleDoubleProperty amountProperty() { return amount; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty statusProperty() { return status; }
    }

    public static class Schedule {
        private final SimpleStringProperty date;
        private final SimpleStringProperty time;
        private final SimpleStringProperty activity;
        private final SimpleStringProperty trainer;
        private final SimpleIntegerProperty participants;
        private final SimpleStringProperty status;

        public Schedule(String date, String time, String activity, String trainer, int participants, String status) {
            this.date = new SimpleStringProperty(date);
            this.time = new SimpleStringProperty(time);
            this.activity = new SimpleStringProperty(activity);
            this.trainer = new SimpleStringProperty(trainer);
            this.participants = new SimpleIntegerProperty(participants);
            this.status = new SimpleStringProperty(status);
        }

        public String getDate() { return date.get(); }
        public String getTime() { return time.get(); }
        public String getActivity() { return activity.get(); }
        public String getTrainer() { return trainer.get(); }
        public int getParticipants() { return participants.get(); }
        public String getStatus() { return status.get(); }

        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty timeProperty() { return time; }
        public SimpleStringProperty activityProperty() { return activity; }
        public SimpleStringProperty trainerProperty() { return trainer; }
        public SimpleIntegerProperty participantsProperty() { return participants; }
        public SimpleStringProperty statusProperty() { return status; }
    }
}