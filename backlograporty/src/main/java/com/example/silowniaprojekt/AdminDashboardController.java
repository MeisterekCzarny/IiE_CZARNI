package com.example.silowniaprojekt;

import com.gymreports.api.*;

// Importy Java (jeśli ich nie ma)
import java.util.List;
import java.util.ArrayList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.Control;


import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import java.util.function.Consumer;

/**
 * Kontroler panelu administratora siłowni.
 * Zarządza funkcjami administracyjnymi systemu, takimi jak zarządzanie użytkownikami,
 * sprzętem, karnetami, produktami, treningami i transakcjami.
 * Implementuje logikę dla wszystkich działań dostępnych w interfejsie panelu administratora.
 */
public class AdminDashboardController {

    // Sekcje
    @FXML private VBox dashboardSection;
    @FXML private VBox usersSection;
    @FXML private VBox trainingsSection;
    @FXML private VBox storeSection;
    @FXML private VBox settingsSection;
    @FXML private VBox reportsSection;

    // Nawigacja
    @FXML private Button dashboardBtn;
    @FXML private Button usersBtn;
    @FXML private Button trainingsBtn;
    @FXML private Button storeBtn;
    @FXML private Button reportsBtn;
    @FXML private Button membershipsBtn;
    @FXML private Button transactionsBtn;
    @FXML
    public Button deleteUserButton;
    @FXML
    public Button editUserButton;
    @FXML
    public Button changePasswordButton;

    // Dashboard
    @FXML
    public LineChart<String, Number> activityChart;

    // Użytkownicy
    @FXML
    public TableView<User> usersTable;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userStatusColumn; // DODAJ TĘ LINIĘ

    // Treningi
    @FXML
    public TableView<Training> trainingsTable;
    @FXML private TableColumn<Training, String> trainingDateColumn;
    @FXML private TableColumn<Training, String> trainingNotesColumn;
    @FXML private TableColumn<Training, String> trainingClientColumn;
    @FXML private TableColumn<Training, String> trainingTrainerColumn;



    // Sklep
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, Double> productPriceColumn;
    @FXML private TableColumn<Product, Integer> productStockColumn;

    // Sprzęt

    // Dodaj te deklaracje do sekcji pól w AdminDashboardController
    @FXML private ComboBox<String> productsPeriodComboBox;
    @FXML private ComboBox<String> membershipPaymentsPeriodComboBox;
    @FXML private ComboBox<String> transactionsPeriodComboBox;

    // Raporty
    @FXML private ComboBox<String> financialPeriodComboBox;


    // Dane
    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<Training> trainings = FXCollections.observableArrayList();
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<Membership> memberships = FXCollections.observableArrayList();
    public ObservableList<Transaction> transactions = FXCollections.observableArrayList();


    private final com.gymreports.api.ReportGenerator reportGenerator = new com.gymreports.impl.ReportGenerator();


    @FXML private VBox membershipsSection;
    @FXML private TableView<Membership> membershipsTable;
    @FXML private TableColumn<Membership, String> membershipClientColumn;
    @FXML private TableColumn<Membership, Double> membershipAmountColumn;
    @FXML private TableColumn<Membership, String> membershipDateColumn;
    // Transakcje
    @FXML private VBox transactionsSection;
    @FXML
    public TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> transactionIdColumn;
    @FXML private TableColumn<Transaction, String> transactionClientColumn;
    @FXML private TableColumn<Transaction, String> transactionProductColumn;
    @FXML private TableColumn<Transaction, String> transactionDateColumn;
    @FXML private TableColumn<Transaction, Double> transactionAmountColumn;

    // Pola do szczegółów treningu
    @FXML private VBox trainingDetailsContainer;
    @FXML private Label detailTrainingDate;
    @FXML private Label detailTrainingClient;
    @FXML private Label detailTrainingTrainer;
    @FXML private TextArea detailTrainingNotes;
    public int userId;
    private int currentUserId;
    private String currentUserEmail;
    /** Nazwa zalogowanego użytkownika */
    private String userName;

    /** Email administratora */
    private String adminEmail;

    // NOWE POLA DLA FILTROWANIA RAPORTÓW
    @FXML private ComboBox<String> productFilterCombo;
    @FXML private ComboBox<String> periodComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private RadioButton customDatesRadio;
    @FXML private RadioButton predefinedPeriodsRadio;
    @FXML private ToggleGroup periodToggleGroup;



    @FXML
    public void initialize() {
        initChart();
        initTables();
        loadDataFromDatabase();
        showDashboard();
        initializeReportComboBoxes();
        initializeReportFilters(); // Dodana nowa metoda


// Dodaj listener do tabeli użytkowników
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Blokuj przyciski jeśli to własne konto
                boolean isSelf = newVal.getId() == this.userId;
                deleteUserButton.setDisable(isSelf);
                editUserButton.setDisable(isSelf);
                changePasswordButton.setDisable(isSelf);
            }
        });



        // Dodaj listener do zaznaczenia w tabeli treningów
        trainingsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showTrainingDetails(newSelection);
                    }
                }
        );
    }

    // NOWA METODA - inicjalizacja filtrów raportów
    private void initializeReportFilters() {
        // Inicjalizacja ComboBox produktów
        if (productFilterCombo != null) {
            loadProductFilterOptions();
        }

        // Inicjalizacja logiki przełączania między trybami dat
        if (predefinedPeriodsRadio != null && customDatesRadio != null) {
            // Domyślnie wybierz predefiniowane okresy
            predefinedPeriodsRadio.setSelected(true);
            updateDatePickersState();

            // Dodaj listenery do RadioButton'ów
            predefinedPeriodsRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateDatePickersState();
            });

            customDatesRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateDatePickersState();
            });
        }

        // Ustaw domyślne daty
        if (startDatePicker != null && endDatePicker != null) {
            endDatePicker.setValue(LocalDate.now());
            startDatePicker.setValue(LocalDate.now().minusMonths(1));
        }
    }

    // NOWA METODA - ładowanie produktów do filtra
    private void loadProductFilterOptions() {
        productFilterCombo.getItems().clear();
        productFilterCombo.getItems().add("Wszystkie");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT name FROM products ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productFilterCombo.getItems().add(rs.getString("name"));
            }

            productFilterCombo.setValue("Wszystkie");

        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania produktów do filtra: " + e.getMessage());
        }
    }

    // NOWA METODA - kontrola stanu DatePicker'ów
    private void updateDatePickersState() {
        boolean customDatesSelected = customDatesRadio != null && customDatesRadio.isSelected();

        if (startDatePicker != null) {
            startDatePicker.setDisable(!customDatesSelected);
        }
        if (endDatePicker != null) {
            endDatePicker.setDisable(!customDatesSelected);
        }
        if (periodComboBox != null) {
            periodComboBox.setDisable(customDatesSelected);
        }
    }

    // NOWA METODA - odświeżanie filtrów
    @FXML
    private void refreshFilters() {
        loadProductFilterOptions();
        showAlert("Informacja", "Filtry zostały odświeżone");
    }

    public void showTrainingDetails(Training training) {
        trainingDetailsContainer.setVisible(true);
        detailTrainingDate.setText(training.getTrainingDate());
        detailTrainingClient.setText(training.getClientName());
        detailTrainingTrainer.setText(training.getTrainerName());
        detailTrainingNotes.setText(training.getNotes());
    }

    public void initializeReportComboBoxes() {
        // Raport finansowy
        if (financialPeriodComboBox != null) {
            financialPeriodComboBox.getItems().clear();
            financialPeriodComboBox.getItems().addAll(
                    "Ostatni miesiąc", "Ostatni kwartał", "Bieżący rok", "Wszystkie"
            );
            financialPeriodComboBox.setValue("Ostatni miesiąc");
        }

        // Główny ComboBox okresów
        if (periodComboBox != null) {
            periodComboBox.getItems().clear();
            periodComboBox.getItems().addAll(
                    "Ostatni tydzień", "Ostatni miesiąc", "Ostatni kwartał", "Bieżący rok", "Wszystkie"
            );
            periodComboBox.setValue("Ostatni miesiąc");
        }

        // Raport produktów
        if (productsPeriodComboBox != null) {
            productsPeriodComboBox.getItems().clear();
            productsPeriodComboBox.getItems().addAll(
                    "Bieżący stan", "Ostatni miesiąc", "Ostatni kwartał", "Bieżący rok"
            );
            productsPeriodComboBox.setValue("Bieżący stan");
        }

        // Raport płatności członkowskich
        if (membershipPaymentsPeriodComboBox != null) {
            membershipPaymentsPeriodComboBox.getItems().clear();
            membershipPaymentsPeriodComboBox.getItems().addAll(
                    "Ostatni miesiąc", "Ostatni kwartał", "Bieżący rok", "Wszystkie"
            );
            membershipPaymentsPeriodComboBox.setValue("Ostatni miesiąc");
        }

        // Raport transakcji
        if (transactionsPeriodComboBox != null) {
            transactionsPeriodComboBox.getItems().clear();
            transactionsPeriodComboBox.getItems().addAll(
                    "Ostatni tydzień", "Ostatni miesiąc", "Ostatni kwartał", "Bieżący rok", "Wszystkie"
            );
            transactionsPeriodComboBox.setValue("Ostatni miesiąc");
        }
    }

    public void initTables() {
        // Użytkownicy
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        userStatusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String status = user.getIsActive() ? "Aktywny" : "Nieaktywny";
            return new SimpleStringProperty(status);
        });

        usersTable.setItems(users);



        // Treningi
        trainingDateColumn.setCellValueFactory(new PropertyValueFactory<>("trainingDate"));
        trainingDateColumn.setCellFactory(tc -> new TableCell<Training, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                }
            }
        });

        trainingNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        trainingNotesColumn.setCellFactory(tc -> {
            TableCell<Training, String> cell = new TableCell<>() {
                private Label label;

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (label == null) {
                            label = new Label();
                            label.setWrapText(true);
                            label.setStyle("-fx-text-fill: white;");
                        }
                        label.setText(item);
                        setGraphic(label);
                    }
                }
            };
            return cell;
        });

        trainingClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        trainingTrainerColumn.setCellValueFactory(new PropertyValueFactory<>("trainerName"));

        trainingsTable.setRowFactory(tv -> {
            TableRow<Training> row = new TableRow<>();
            row.setPrefHeight(Control.USE_COMPUTED_SIZE);
            row.setMinHeight(Control.USE_COMPUTED_SIZE);
            row.setMaxHeight(Control.USE_COMPUTED_SIZE);
            return row;
        });
        trainingsTable.setItems(trainings);

        // Produkty
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Cena produktu - wyrównanie do prawej
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productPriceColumn.setCellFactory(tc -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("%.2f zł", price)); // Dodano "zł"
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                }
            }
        });

        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        productStockColumn.setCellFactory(tc -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(stock));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                }
            }
        });
        productsTable.setItems(products);

        // Sprzęt


        // Karnety
        membershipClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));

        // Kwota karnetu - wyrównanie do prawej
        membershipAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        membershipAmountColumn.setCellFactory(tc -> new TableCell<Membership, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f zł", amount)); // Dodano "zł"
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                }
            }
        });

        membershipDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        // Data płatności (wyrównanie)
        membershipDateColumn.setCellFactory(tc -> new TableCell<Membership, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                }
            }
        });
        membershipsTable.setItems(memberships);

        // Transakcje
        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        transactionClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        transactionProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        // Data płatności (wyrównanie)
        transactionDateColumn.setCellFactory(tc -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                }
            }
        });;


        // Kwota transakcji - wyrównanie do prawej
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionAmountColumn.setCellFactory(tc -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f zł", item));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 15px 0 0;");
                }
            }
        });


        transactionsTable.setItems(transactions);

    }

    public void initChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Aktywność klientów");
        series.getData().addAll(
                new XYChart.Data<>("Pon", 65),
                new XYChart.Data<>("Wt", 59),
                new XYChart.Data<>("Śr", 80),
                new XYChart.Data<>("Czw", 81),
                new XYChart.Data<>("Pt", 56),
                new XYChart.Data<>("Sob", 55),
                new XYChart.Data<>("Nd", 40)
        );
        activityChart.getData().add(series);
    }

    public void loadDataFromDatabase() {
        loadUsers();
        loadTrainings();
        loadProducts();
        loadMemberships();
        loadTransactions();

    }

    // Zmodyfikowana metoda loadUsers()
    private void loadUsers() {
        users.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("id");

                // Pomiń aktualnie zalogowanego użytkownika
                if (userId == this.userId) {
                    continue;
                }

                users.add(new User(
                        userId,
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania użytkowników: " + e.getMessage());
        }
    }

    private void loadTrainings() {
        trainings.clear();
        String query = """
        SELECT tr.id, tr.training_date, tr.notes, 
               c.name AS client_name, t.name AS trainer_name
        FROM trainingrequests tr
        JOIN reports r ON tr.report = r.id
        JOIN users c ON r.client_id = c.id
        JOIN users t ON r.trainer_id = t.id
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                trainings.add(new Training(
                        rs.getInt("id"),
                        rs.getString("training_date"),
                        rs.getString("notes"),
                        rs.getString("client_name"),
                        rs.getString("trainer_name")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania treningów: " + e.getMessage());
        }
    }

    private void loadProducts() {
        products.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania produktów: " + e.getMessage());
        }
    }


    private void loadMemberships() {
        memberships.clear();
        String query = """
        SELECT mp.*, u.name AS client_name 
        FROM membership_payments mp
        JOIN users u ON mp.client_id = u.id
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                memberships.add(new Membership(
                        rs.getInt("id"),
                        rs.getString("client_name"),
                        rs.getDouble("amount"),
                        rs.getDate("payment_date").toString() // Używamy getDate i konwertujemy na String
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania płatności: " + e.getMessage());
        }
    }
    private void loadTransactions() {
        transactions.clear();
        String query = """
            SELECT t.id, u.name AS client_name, p.name AS product_name, 
                   t.transaction_date, t.amount 
            FROM transactions t
            JOIN users u ON t.client_id = u.id
            JOIN products p ON t.product_id = p.id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getInt("id"),
                        rs.getString("client_name"),
                        rs.getString("product_name"),
                        rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate().toString(),
                        rs.getDouble("amount")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania transakcji: " + e.getMessage());
        }
    }


    // Metody pobierające dane z bazy
    private List<TransactionDataImpl> getTransactionsFromDatabase(String period) {
        List<TransactionDataImpl> transactions = new ArrayList<>();
        LocalDate[] dateRange = getDateRangeForPeriod(period);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        String query = """
        SELECT t.id, u.name AS client_name, p.name AS product_name, 
               t.transaction_date, t.amount 
        FROM transactions t
        JOIN users u ON t.client_id = u.id
        JOIN products p ON t.product_id = p.id
        WHERE DATE(t.transaction_date) BETWEEN ? AND ?
        ORDER BY t.transaction_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionDataImpl(
                            rs.getInt("id"),
                            rs.getString("client_name"),
                            rs.getString("product_name"),
                            rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate()
                                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            rs.getDouble("amount")
                    ));
                }
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania transakcji: " + e.getMessage());
        }

        return transactions;
    }

    private List<MembershipDataImpl> getMembershipsFromDatabase(String period) {
        List<MembershipDataImpl> memberships = new ArrayList<>();
        LocalDate[] dateRange = getDateRangeForPeriod(period);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        String query = """
        SELECT mp.id, u.name AS client_name, mp.amount, mp.payment_date
        FROM membership_payments mp
        JOIN users u ON mp.client_id = u.id
        WHERE DATE(mp.payment_date) BETWEEN ? AND ?
        ORDER BY mp.payment_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    memberships.add(new MembershipDataImpl(
                            rs.getInt("id"),
                            rs.getString("client_name"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("payment_date").toLocalDateTime().toLocalDate()
                                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    ));
                }
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania płatności: " + e.getMessage());
        }

        return memberships;
    }

    private List<ProductDataImpl> getProductsFromDatabase() {
        List<ProductDataImpl> products = new ArrayList<>();

        String query = "SELECT id, name, price, stock FROM products ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new ProductDataImpl(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania produktów: " + e.getMessage());
        }

        return products;
    }

    public LocalDate[] getDateRangeForPeriod(String period) {
        if (period.contains(":")) {
            String[] dates = period.split(":");
            try {
                LocalDate startDate = LocalDate.parse(dates[0]);
                LocalDate endDate = LocalDate.parse(dates[1]);
                return new LocalDate[] { startDate, endDate };
            } catch (Exception e) {
            }
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case "Ostatni tydzień":
                startDate = endDate.minusWeeks(1);
                break;
            case "Ostatni miesiąc":
                startDate = endDate.minusMonths(1);
                break;
            case "Ostatni kwartał":
                startDate = endDate.minusMonths(3);
                break;
            case "Bieżący rok":
                startDate = LocalDate.of(endDate.getYear(), 1, 1);
                break;
            case "Wszystkie":
                startDate = LocalDate.of(2000, 1, 1);
                break;
            case "Bieżący stan":
                startDate = endDate;
                break;
            default:
                startDate = endDate.minusMonths(1);
        }

        return new LocalDate[] { startDate, endDate };
    }


    @FXML
    private void editTransaction() {
        Transaction selectedTransaction = transactionsTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert("Błąd", "Wybierz transakcję do edycji");
            return;
        }

        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Edytuj transakcję");
        dialog.setHeaderText("Edytuj transakcję ID: " + selectedTransaction.getId());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        ComboBox<String> clientCombo = new ComboBox<>();

        TextField amountField = new TextField();
        TextFormatter<String> amountFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*([.,]\\d{0,2})?") || newText.isEmpty()) {
                // Zamień przecinki na kropki
                if (change.getText().contains(",")) {
                    change.setText(change.getText().replace(',', '.'));
                }
                return change;
            }
            return null;
        });
        amountField.setTextFormatter(amountFormatter);
        amountField.setText(String.format(Locale.US, "%.2f", selectedTransaction.getAmount()));

        DatePicker datePicker = new DatePicker(LocalDate.parse(selectedTransaction.getTransactionDate()));

        // Załaduj klientów
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users WHERE role = 'client'");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clientCombo.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania klientów: " + e.getMessage());
        }

        // Ustaw aktualnego klienta
        clientCombo.setValue(selectedTransaction.getClientName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Klient:"), 0, 0);
        grid.add(clientCombo, 1, 0);
        grid.add(new Label("Kwota:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Data:"), 0, 2);
        grid.add(datePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    // Pobierz tekst i zamień przecinki na kropki
                    String amountText = amountField.getText().replace(',', '.');
                    double amount = Double.parseDouble(amountText);
                    return new Transaction(
                            selectedTransaction.getId(),
                            clientCombo.getValue(),
                            selectedTransaction.getProductName(),
                            datePicker.getValue().toString(),
                            amount
                    );
                } catch (NumberFormatException e) {
                    showAlert("Błąd", "Nieprawidłowy format kwoty");
                    return null;
                }
            }
            return null;
        });

        Optional<Transaction> result = dialog.showAndWait();

        result.ifPresent(editedTransaction -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Znajdź ID klienta po nazwie
                String clientQuery = "SELECT id FROM users WHERE name = ?";
                PreparedStatement clientStmt = conn.prepareStatement(clientQuery);
                clientStmt.setString(1, editedTransaction.getClientName());
                ResultSet rs = clientStmt.executeQuery();

                if (rs.next()) {
                    int clientId = rs.getInt("id");

                    String updateQuery = "UPDATE transactions SET client_id = ?, amount = ?, transaction_date = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(updateQuery);
                    stmt.setInt(1, clientId);
                    stmt.setDouble(2, editedTransaction.getAmount());
                    stmt.setDate(3, java.sql.Date.valueOf(editedTransaction.getTransactionDate()));
                    stmt.setInt(4, editedTransaction.getId());

                    if (stmt.executeUpdate() > 0) {
                        // Aktualizuj lokalny obiekt
                        selectedTransaction.setClientName(editedTransaction.getClientName());
                        selectedTransaction.setAmount(editedTransaction.getAmount());
                        selectedTransaction.setTransactionDate(editedTransaction.getTransactionDate());

                        transactionsTable.refresh();
                        showAlert("Sukces", "Transakcja została zaktualizowana");
                    }
                } else {
                    showAlert("Błąd", "Nie znaleziono klienta o podanej nazwie");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zaktualizować transakcji: " + e.getMessage());
            }
        });
    }
    @FXML
    public void deleteTransaction() {
        Transaction selectedTransaction = transactionsTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert("Błąd", "Wybierz transakcję do usunięcia");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Potwierdzenie usunięcia");
        confirmation.setHeaderText("Czy na pewno chcesz usunąć transakcję ID: " + selectedTransaction.getId() + "?");
        confirmation.setContentText("Klient: " + selectedTransaction.getClientName() +
                "\nProdukt: " + selectedTransaction.getProductName() +
                "\nKwota: " + String.format("%.2f", selectedTransaction.getAmount()));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM transactions WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedTransaction.getId());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    transactions.remove(selectedTransaction);
                    showAlert("Sukces", "Transakcja została usunięta");
                } else {
                    showAlert("Błąd", "Nie udało się usunąć transakcji");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Błąd bazy danych: " + e.getMessage());
            }
        }
    }

    // Navigacja
    @FXML
    public void showDashboard() { toggleSection(dashboardSection, dashboardBtn); }
    @FXML
    public void showUsers() { toggleSection(usersSection, usersBtn); }
    @FXML
    public void showTrainings() { toggleSection(trainingsSection, trainingsBtn); }
    @FXML
    public void showStore() { toggleSection(storeSection, storeBtn); }
    @FXML
    public void showReports() { toggleSection(reportsSection, reportsBtn); }
    @FXML
    public void showMemberships() { toggleSection(membershipsSection, membershipsBtn); }
    @FXML
    public void showTransactions() {toggleSection(transactionsSection, transactionsBtn);}

    public void toggleSection(VBox section, Button button) {
        hideAllSections();
        section.setVisible(true);
        setActiveButton(button);
    }

    private void hideAllSections() {
        dashboardSection.setVisible(false);
        usersSection.setVisible(false);
        trainingsSection.setVisible(false);
        storeSection.setVisible(false);
        settingsSection.setVisible(false);
        reportsSection.setVisible(false);
        membershipsSection.setVisible(false);
        transactionsSection.setVisible(false); // Dodaj tę linię

    }

    private void setActiveButton(Button activeButton) {
        // Usuń klasę "active" ze wszystkich przycisków
        dashboardBtn.getStyleClass().remove("active");
        usersBtn.getStyleClass().remove("active");
        trainingsBtn.getStyleClass().remove("active");
        storeBtn.getStyleClass().remove("active");
        reportsBtn.getStyleClass().remove("active");
        membershipsBtn.getStyleClass().remove("active");
        transactionsBtn.getStyleClass().remove("active");

        // Dodaj klasę "active" tylko do aktywnego przycisku
        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
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

            // Ustaw scenę logowania
            Scene loginScene = new Scene(root);
            stage.setScene(loginScene);
            stage.setTitle("Black Iron Gym - System logowania");

            // Maksymalizuj okno (pełny ekran z paskiem tytułu)
            stage.setMaximized(true); // <-- to ustawia aplikację na cały ekran

            // (opcjonalnie) przekaż stage do kontrolera logowania
            LoginController controller = loader.getController();
            controller.setLoginStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
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
        passwordField.setPromptText("Hasło (min. 8 znaków)");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("client", "trainer", "employee", "admin");
        roleBox.setValue("client");

        // Etykiety błędów
        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        Label emailError = new Label();
        emailError.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        Label passwordError = new Label();
        passwordError.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");

        // Układ
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5); // Zmniejszona przerwa dla lepszego układu
        grid.add(new Label("Imię i nazwisko:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(nameError, 1, 1); // Błąd pod polem
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(emailError, 1, 3); // Błąd pod polem
        grid.add(new Label("Hasło:"), 0, 4);
        grid.add(passwordField, 1, 4);
        grid.add(passwordError, 1, 5); // Błąd pod polem
        grid.add(new Label("Rola:"), 0, 6);
        grid.add(roleBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Początkowo przycisk "Dodaj" jest wyłączony
        Node addButtonNode = dialog.getDialogPane().lookupButton(addButton);
        addButtonNode.setDisable(true);

        // Funkcja walidująca wszystkie pola
        Runnable validateFields = () -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();

            // Resetuj komunikaty błędów
            nameError.setText("");
            emailError.setText("");
            passwordError.setText("");

            boolean isValid = true;

            // Walidacja imienia i nazwiska
            if (name == null || name.trim().isEmpty()) {
                nameError.setText("Pole wymagane");
                isValid = false;
            } else if (name.matches(".*\\d.*")) {
                nameError.setText("Nie może zawierać cyfr");
                isValid = false;
            }

            // Walidacja emaila
            if (email == null || email.trim().isEmpty()) {
                emailError.setText("Pole wymagane");
                isValid = false;
            } else if (!email.matches(".+@.+\\..+")) {
                emailError.setText("Nieprawidłowy format email");
                isValid = false;
            }

            // Walidacja hasła
            if (password == null || password.isEmpty()) {
                passwordError.setText("Pole wymagane");
                isValid = false;
            } else if (password.length() < 8) {
                passwordError.setText("Hasło musi mieć min. 8 znaków");
                isValid = false;
            }

            addButtonNode.setDisable(!isValid);
        };

        // Dodaj listenery do pól
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                return new User(0,
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        roleBox.getValue(),
                        true
                );
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(user -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    String query = "INSERT INTO users (name, email, password, role, is_active) VALUES (?, ?, ?, ?, TRUE)";                    PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, user.getName());
                    stmt.setString(2, user.getEmail());

                    // HASHOWANIE HASŁA
                    String plainPassword = passwordField.getText();
                    String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                    stmt.setString(3, hashedPassword);

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
                user.setId(users.size() + 1);
                users.add(user);
                showAlert("Uwaga", "Użytkownik dodany lokalnie z powodu błędu bazy.");
            }
        });
    }

    @FXML
    private void editUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Błąd", "Wybierz użytkownika do edycji");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edytuj użytkownika");
        dialog.setHeaderText("Edytuj dane użytkownika: " + selectedUser.getName());

        // Ustaw minimalną szerokość okna
        dialog.getDialogPane().setMinWidth(500);

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // Pola formularza
        TextField nameField = new TextField(selectedUser.getName());
        TextField emailField = new TextField(selectedUser.getEmail());
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList(
                "client", "trainer", "employee", "admin"
        ));
        roleBox.setValue(selectedUser.getRole());

        // Etykiety błędów
        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red; -fx-font-size: 10px; -fx-wrap-text: true;");
        Label emailError = new Label();
        emailError.setStyle("-fx-text-fill: red; -fx-font-size: 10px; -fx-wrap-text: true;");

        // Układ - teraz z własnym kontenerem dla każdego pola
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        // Wiersz 0: Imię i nazwisko
        grid.add(new Label("Imię i nazwisko:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(nameError, 1, 1); // Dodaj etykietę błędu w nowym wierszu pod polem

        // Wiersz 2: Email
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(emailError, 1, 3); // Dodaj etykietę błędu w nowym wierszu pod polem

        // Wiersz 4: Rola
        grid.add(new Label("Rola:"), 0, 4);
        grid.add(roleBox, 1, 4);

        // Ustaw preferowaną szerokość pól
        nameField.setPrefWidth(150);
        emailField.setPrefWidth(150);
        roleBox.setPrefWidth(150);

        // Ustaw kontener jako zawartość
        dialog.getDialogPane().setContent(grid);

        // Walidacja w czasie rzeczywistym
        Node saveButtonNode = dialog.getDialogPane().lookupButton(saveButton);
        saveButtonNode.setDisable(false);

        // Walidacja nazwy: nie puste, bez cyfr
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                nameError.setText("Pole wymagane");
                updateSaveButtonState(saveButtonNode, nameField, emailField, nameError, emailError);
            } else if (newVal.matches(".*\\d.*")) {
                nameError.setText("Nie może zawierać cyfr");
                updateSaveButtonState(saveButtonNode, nameField, emailField, nameError, emailError);
            } else {
                nameError.setText("");
                updateSaveButtonState(saveButtonNode, nameField, emailField, nameError, emailError);
            }
        });

        // Walidacja emaila: nie pusty, poprawny format
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                emailError.setText("Pole wymagane");
                updateSaveButtonState(saveButtonNode, nameField, emailField, nameError, emailError);
            } else if (!newVal.matches(".+@.+\\..+")) {
                emailError.setText("Nieprawidłowy format email (przykład: user@domena.pl)");
                updateSaveButtonState(saveButtonNode, nameField, emailField, nameError, emailError);
            } else {
                emailError.setText("");
                updateSaveButtonState(saveButtonNode, nameField, emailField, nameError, emailError);
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return new User(
                        selectedUser.getId(),
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        roleBox.getValue(),
                        true
                );
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(editedUser -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE users SET name = ?, email = ?, role = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, editedUser.getName());
                stmt.setString(2, editedUser.getEmail());
                stmt.setString(3, editedUser.getRole());
                stmt.setInt(4, editedUser.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedUser.setName(editedUser.getName());
                    selectedUser.setEmail(editedUser.getEmail());
                    selectedUser.setRole(editedUser.getRole());
                    usersTable.refresh();
                    showAlert("Sukces", "Dane użytkownika zostały zaktualizowane");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zaktualizować użytkownika: " + e.getMessage());
            }
        });
    }

    // Metoda pomocnicza do aktualizacji stanu przycisku "Zapisz"
    private void updateSaveButtonState(Node saveButtonNode, TextField nameField, TextField emailField, Label nameError, Label emailError) {
        boolean nameValid = nameField.getText() != null &&
                !nameField.getText().trim().isEmpty() &&
                !nameField.getText().matches(".*\\d.*") &&
                nameError.getText().isEmpty();

        boolean emailValid = emailField.getText() != null &&
                !emailField.getText().trim().isEmpty() &&
                emailField.getText().matches(".+@.+\\..+") &&
                emailError.getText().isEmpty();

        saveButtonNode.setDisable(!(nameValid && emailValid));
    }

    @FXML
    private void deleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Błąd", "Wybierz użytkownika do deaktywacji!");
            return;
        }

        if (selectedUser.getId() == currentUserId ||
                (selectedUser.getEmail() != null && selectedUser.getEmail().equals(currentUserEmail))) {
            showAlert("Błąd", "Nie możesz dezaktywować własnego konta");
            return;
        }

        // Ostrzeżenie dla administratora, ale pozwalamy kontynuować
        if ("admin".equals(selectedUser.getRole())) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Ostrzeżenie");
            warning.setHeaderText("Dezaktywujesz innego administratora!");
            warning.setContentText("Czy na pewno chcesz kontynuować?");
            if (warning.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        // Potwierdzenie
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potwierdzenie");
        confirm.setHeaderText("Czy na pewno chcesz dezaktywować użytkownika " + selectedUser.getName() + "?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE users SET is_active = FALSE WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedUser.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedUser.setIsActive(false);
                    usersTable.refresh();
                    showAlert("Sukces", "Użytkownik został dezaktywowany.");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Błąd dezaktywacji: " + e.getMessage());
            }
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

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setCurrentUser(int id, String email) {
        this.currentUserId = id;
        this.currentUserEmail = email;
        System.out.println("Admin ID: " + id + ", Email: " + email); // do debugowania

    }

    @FXML
    private void generateFinancialReport() {
        try {
            String period = getPeriodString();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz Raport Finansowy");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            fileChooser.setInitialFileName("raport_finansowy_" + LocalDate.now() + ".pdf");

            Stage stage = (Stage) reportsSection.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String userEmail = getCurrentUserEmail();

                // NOWY KOD - używa biblioteki
                List<TransactionDataImpl> transactions = getTransactionsFromDatabase(period);
                List<MembershipDataImpl> memberships = getMembershipsFromDatabase(period);

                ReportConfig config = ReportConfig.forPeriod(period, userEmail)
                        .withTransactions(new ArrayList<>(transactions))
                        .withMemberships(new ArrayList<>(memberships));

                reportGenerator.generateFinancialReport(config, file);
                showAlert("Sukces", "Raport finansowy został wygenerowany!");
            }
        } catch (ReportGenerationException e) {
            showError("Błąd generowania raportu: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void generateProductsReport() {
        try {
            String period = getPeriodString();
            String selectedProduct = productFilterCombo != null ?
                    productFilterCombo.getSelectionModel().getSelectedItem() : "Wszystkie";

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz Raport Produktów");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            String fileName = selectedProduct != null && !selectedProduct.equals("Wszystkie") ?
                    "raport_produktow_" + selectedProduct.replaceAll("\\s+", "_") + "_" + LocalDate.now() + ".pdf" :
                    "raport_produktow_" + LocalDate.now() + ".pdf";
            fileChooser.setInitialFileName(fileName);

            Stage stage = (Stage) reportsSection.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String userEmail = getCurrentUserEmail();

                // ZMIENIONY KOD - filtrujemy transakcje według produktu
                List<ProductDataImpl> products = getProductsFromDatabase();
                List<TransactionDataImpl> transactions = getTransactionsFromDatabaseForProduct(period, selectedProduct);

                ReportConfig config = ReportConfig.forPeriod(period, userEmail)
                        .withProducts(new ArrayList<>(products))
                        .withTransactions(new ArrayList<>(transactions))
                        .withSelectedProduct(selectedProduct);

                reportGenerator.generateProductsReport(config, file);
                showAlert("Sukces", "Raport produktów został wygenerowany!");
            }
        } catch (ReportGenerationException e) {
            showError("Błąd generowania raportu: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<TransactionDataImpl> getTransactionsFromDatabaseForProduct(String period, String selectedProduct) {
        List<TransactionDataImpl> transactions = new ArrayList<>();
        LocalDate[] dateRange = getDateRangeForPeriod(period);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        String query;
        boolean filterByProduct = selectedProduct != null && !selectedProduct.equals("Wszystkie");

        if (filterByProduct) {
            query = """
        SELECT t.id, u.name AS client_name, p.name AS product_name, 
               t.transaction_date, t.amount 
        FROM transactions t
        JOIN users u ON t.client_id = u.id
        JOIN products p ON t.product_id = p.id
        WHERE DATE(t.transaction_date) BETWEEN ? AND ? AND p.name = ?
        ORDER BY t.transaction_date DESC
        """;
        } else {
            query = """
        SELECT t.id, u.name AS client_name, p.name AS product_name, 
               t.transaction_date, t.amount 
        FROM transactions t
        JOIN users u ON t.client_id = u.id
        JOIN products p ON t.product_id = p.id
        WHERE DATE(t.transaction_date) BETWEEN ? AND ?
        ORDER BY t.transaction_date DESC
        """;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());

            if (filterByProduct) {
                stmt.setString(3, selectedProduct);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionDataImpl(
                            rs.getInt("id"),
                            rs.getString("client_name"),
                            rs.getString("product_name"),
                            rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate()
                                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            rs.getDouble("amount")
                    ));
                }
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania transakcji: " + e.getMessage());
        }

        return transactions;
    }

    @FXML
    private void generateMembershipReport() {
        try {
            String period = getPeriodString();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz Raport Karnetów");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            fileChooser.setInitialFileName("raport_karnetow_" + LocalDate.now() + ".pdf");

            Stage stage = (Stage) reportsSection.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String userEmail = getCurrentUserEmail();

                // NOWY KOD - używa biblioteki
                List<MembershipDataImpl> memberships = getMembershipsFromDatabase(period);

                ReportConfig config = ReportConfig.forPeriod(period, userEmail)
                        .withMemberships(new ArrayList<>(memberships));

                reportGenerator.generateMembershipReport(config, file);
                showAlert("Sukces", "Raport karnetów został wygenerowany!");
            }
        } catch (ReportGenerationException e) {
            showError("Błąd generowania raportu: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void generateTransactionsReport() {
        try {
            String period = getPeriodString();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Zapisz Raport Transakcji");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            fileChooser.setInitialFileName("raport_transakcji_" + LocalDate.now() + ".pdf");

            Stage stage = (Stage) reportsSection.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                String userEmail = getCurrentUserEmail();

                // NOWY KOD - używa biblioteki
                List<TransactionDataImpl> transactions = getTransactionsFromDatabase(period);

                ReportConfig config = ReportConfig.forPeriod(period, userEmail)
                        .withTransactions(new ArrayList<>(transactions));

                reportGenerator.generateTransactionsReport(config, file);
                showAlert("Sukces", "Raport transakcji został wygenerowany!");
            }
        } catch (ReportGenerationException e) {
            showError("Błąd generowania raportu: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pobiera string reprezentujący wybrany okres czasowy dla raportów.
     *
     * @return string okresu w formacie odpowiednim dla generatora raportów
     * @throws IllegalArgumentException gdy daty są nieprawidłowe
     */
    private String getPeriodString() {
        try {
            if (customDatesRadio != null && customDatesRadio.isSelected()) {
                LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : null;
                LocalDate endDate = endDatePicker != null ? endDatePicker.getValue() : null;

                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException("Wybierz daty początkową i końcową");
                }

                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("Data początkowa nie może być późniejsza niż końcowa");
                }

                return startDate.toString() + ":" + endDate.toString();
            } else {
                String selectedPeriod = periodComboBox != null ?
                        periodComboBox.getSelectionModel().getSelectedItem() : null;
                if (selectedPeriod == null || selectedPeriod.isEmpty()) {
                    selectedPeriod = "Ostatni miesiąc";
                }
                return selectedPeriod;
            }
        } catch (Exception e) {
            showAlert("Błąd", "Problem z wyborem okresu: " + e.getMessage());
            return "Ostatni miesiąc"; // domyślna wartość
        }
    }

    private String getCurrentUserEmail() {
        return this.adminEmail != null ? this.adminEmail : "admin@blackirongym.com";
    }

    /**
     * Wyświetla alert błędu.
     *
     * @param message treść komunikatu błędu
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void activateUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Błąd", "Wybierz użytkownika do aktywacji");
            return;
        }

        if (selectedUser.getIsActive()) {
            showAlert("Informacja", "Użytkownik jest już aktywny");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET is_active = TRUE WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, selectedUser.getId());

            if (stmt.executeUpdate() > 0) {
                selectedUser.setIsActive(true);
                usersTable.refresh();
                showAlert("Sukces", "Użytkownik został aktywowany");
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Nie udało się aktywować użytkownika: " + e.getMessage());
        }
    }

    @FXML
    private void changePassword() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Błąd", "Wybierz użytkownika do zmiany hasła");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Zmiana hasła");
        dialog.setHeaderText("Zmiana hasła dla: " + selectedUser.getName());

        ButtonType changeButton = new ButtonType("Zmień", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButton, ButtonType.CANCEL);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nowe hasło (min. 8 znaków)");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Potwierdź hasło");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowe hasło:"), 0, 0);
        grid.add(newPasswordField, 1, 0);
        grid.add(new Label("Potwierdź hasło:"), 0, 1);
        grid.add(confirmPasswordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Walidacja
        Node changeButtonNode = dialog.getDialogPane().lookupButton(changeButton);
        changeButtonNode.setDisable(true);

        Consumer<String> validatePasswords = (value) -> {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            boolean isValid = newPassword.length() >= 8 &&
                    newPassword.equals(confirmPassword);

            changeButtonNode.setDisable(!isValid);
        };

        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswords.accept(newVal));
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswords.accept(newVal));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButton) {
                return newPasswordField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newPassword -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // WAŻNE: Sprawdź połączenie
                if (conn == null) {
                    showAlert("Błąd", "Brak połączenia z bazą danych");
                    return;
                }

                String query = "UPDATE users SET password = ? WHERE id = ? AND is_active = TRUE";
                PreparedStatement stmt = conn.prepareStatement(query);

                // Bezpieczne hashowanie hasła z obsługą błędów
                String hashedPassword;
                try {
                    hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                } catch (IllegalArgumentException e) {
                    showAlert("Błąd", "Nieprawidłowe hasło: " + e.getMessage());
                    return;
                }

                stmt.setString(1, hashedPassword);
                stmt.setInt(2, selectedUser.getId());

                int updatedRows = stmt.executeUpdate();

                if (updatedRows > 0) {
                    showAlert("Sukces", "Hasło zostało zmienione");

                    // DEBUG: Wyświetl hash w konsoli do weryfikacji
                    System.out.println("Zmieniono hasło dla użytkownika: " + selectedUser.getEmail());
                    System.out.println("Nowy hash: " + hashedPassword);
                } else {
                    showAlert("Błąd", "Nie udało się zmienić hasła. Brak zmian w bazie.");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Błąd bazy danych: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void changeTrainingDate() {
        Training selectedTraining = trainingsTable.getSelectionModel().getSelectedItem();
        if (selectedTraining == null) {
            showAlert("Błąd", "Wybierz trening do zmiany daty");
            return;
        }

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Zmiana daty treningu");
        dialog.setHeaderText("Zmiana daty treningu dla: " + selectedTraining.getClientName());

        // Przyciski
        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // DatePicker z aktualną datą
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.parse(selectedTraining.getTrainingDate()));

        // Układ
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowa data:"), 0, 0);
        grid.add(datePicker, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return datePicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();

        result.ifPresent(newDate -> {
            // Aktualizuj datę w bazie danych
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE trainingrequests SET training_date = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, newDate.toString());
                stmt.setInt(2, selectedTraining.getId());

                if (stmt.executeUpdate() > 0) {
                    // Aktualizuj lokalnie
                    selectedTraining.setTrainingDate(newDate.toString());
                    trainingsTable.refresh();
                    showAlert("Sukces", "Data treningu została zmieniona");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zaktualizować daty: " + e.getMessage());
            }
        });
    }

    @FXML
    private void editTrainingNotes() {
        Training selectedTraining = trainingsTable.getSelectionModel().getSelectedItem();
        if (selectedTraining == null) {
            showAlert("Błąd", "Wybierz trening do edycji notatek");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edycja notatek");
        dialog.setHeaderText("Edycja notatek dla treningu: " + selectedTraining.getClientName());

        // Przyciski
        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // TextArea z aktualnymi notatkami
        TextArea notesArea = new TextArea(selectedTraining.getNotes());
        notesArea.setPrefRowCount(5);

        // Układ
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Notatki:"), 0, 0);
        grid.add(notesArea, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return notesArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newNotes -> {
            // Aktualizuj notatki w bazie danych
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE trainingrequests SET notes = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, newNotes);
                stmt.setInt(2, selectedTraining.getId());

                if (stmt.executeUpdate() > 0) {
                    // Aktualizuj lokalnie
                    selectedTraining.setNotes(newNotes);
                    trainingsTable.refresh();
                    showAlert("Sukces", "Notatki zostały zaktualizowane");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zaktualizować notatek: " + e.getMessage());
            }
        });
    }

    @FXML
    private void changeTrainingTrainer() {
        Training selectedTraining = trainingsTable.getSelectionModel().getSelectedItem();
        if (selectedTraining == null) {
            showAlert("Błąd", "Wybierz trening do zmiany trenera");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Zmiana trenera");
        dialog.setHeaderText("Zmiana trenera dla: " + selectedTraining.getClientName());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        ComboBox<String> trainerComboBox = new ComboBox<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users WHERE role = 'trainer'");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                trainerComboBox.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Błąd", "Błąd ładowania trenerów: " + e.getMessage());
        }

        trainerComboBox.setValue(selectedTraining.getTrainerName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowy trener:"), 0, 0);
        grid.add(trainerComboBox, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return trainerComboBox.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newTrainer -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Pobierz ID trenera po nazwie
                String trainerQuery = "SELECT id FROM users WHERE name = ? AND role = 'trainer'";
                PreparedStatement trainerStmt = conn.prepareStatement(trainerQuery);
                trainerStmt.setString(1, newTrainer);
                ResultSet trainerRs = trainerStmt.executeQuery();

                if (trainerRs.next()) {
                    int trainerId = trainerRs.getInt("id");

                    // Pobierz ID raportu powiązanego z treningiem
                    String reportQuery = "SELECT report FROM trainingrequests WHERE id = ?";
                    PreparedStatement reportStmt = conn.prepareStatement(reportQuery);
                    reportStmt.setInt(1, selectedTraining.getId());
                    ResultSet reportRs = reportStmt.executeQuery();

                    if (reportRs.next()) {
                        int reportId = reportRs.getInt("report");

                        // Aktualizuj raport
                        String updateQuery = "UPDATE reports SET trainer_id = ? WHERE id = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setInt(1, trainerId);
                        updateStmt.setInt(2, reportId);

                        if (updateStmt.executeUpdate() > 0) {
                            selectedTraining.setTrainerName(newTrainer);
                            trainingsTable.refresh();
                            showAlert("Sukces", "Trener został zmieniony");
                        }
                    } else {
                        showAlert("Błąd", "Nie znaleziono raportu dla treningu");
                    }
                } else {
                    showAlert("Błąd", "Nie znaleziono trenera o podanej nazwie");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zmienić trenera: " + e.getMessage());
            }
        });
    }

    @FXML
    private void changeMembershipAmount() {
        Membership selectedMembership = membershipsTable.getSelectionModel().getSelectedItem();
        if (selectedMembership == null) {
            showAlert("Błąd", "Wybierz karnet do zmiany kwoty");
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Zmiana kwoty karnetu");
        dialog.setHeaderText("Zmiana kwoty karnetu dla: " + selectedMembership.getClientName());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField amountField = new TextField(String.format("%.2f", selectedMembership.getAmount()));
        amountField.setPromptText("Nowa kwota (format: 0.00)");

        // Walidacja kwoty: tylko liczby i kropka, maksymalnie dwa miejsca po przecinku
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                amountField.setText(oldValue);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowa kwota:"), 0, 0);
        grid.add(amountField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    showAlert("Błąd", "Nieprawidłowy format kwoty");
                    return null;
                }
            }
            return null;
        });

        Optional<Double> result = dialog.showAndWait();

        result.ifPresent(newAmount -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE membership_payments SET amount = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDouble(1, newAmount);
                stmt.setInt(2, selectedMembership.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedMembership.setAmount(newAmount);
                    membershipsTable.refresh();
                    showAlert("Sukces", "Kwota karnetu została zmieniona");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zmienić kwoty: " + e.getMessage());
            }
        });
    }



    @FXML
    private void changeMembershipDate() {
        Membership selectedMembership = membershipsTable.getSelectionModel().getSelectedItem();
        if (selectedMembership == null) {
            showAlert("Błąd", "Wybierz karnet do zmiany daty");
            return;
        }

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Zmiana daty karnetu");
        dialog.setHeaderText("Zmiana daty karnetu dla: " + selectedMembership.getClientName());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.parse(selectedMembership.getPaymentDate()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nowa data:"), 0, 0);
        grid.add(datePicker, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return datePicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();

        result.ifPresent(newDate -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE membership_payments SET payment_date = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDate(1, java.sql.Date.valueOf(newDate));
                stmt.setInt(2, selectedMembership.getId());

                if (stmt.executeUpdate() > 0) {
                    selectedMembership.setPaymentDate(newDate.toString());
                    membershipsTable.refresh();
                    showAlert("Sukces", "Data karnetu została zmieniona");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zmienić daty: " + e.getMessage());
            }
        });
    }

    @FXML
    private void editProduct() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Błąd", "Wybierz produkt do edycji");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edytuj produkt");
        dialog.setHeaderText("Edytuj dane produktu: " + selectedProduct.getName());

        ButtonType saveButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // Pola formularza
        TextField nameField = new TextField(selectedProduct.getName());
        nameField.setEditable(false);

        TextField priceField = new TextField(String.format("%.2f", selectedProduct.getPrice()));
        priceField.setPromptText("Cena (format: 0.00)");

        TextField stockField = new TextField(String.valueOf(selectedProduct.getStock()));
        stockField.setPromptText("Ilość w magazynie");

        // Walidacja ceny: tylko liczby i kropka, maksymalnie dwa miejsca po przecinku
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                priceField.setText(oldValue);
            }
        });

        // Układ
        GridPane grid = new GridPane();
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
            if (dialogButton == saveButton) {
                try {
                    double price = Double.parseDouble(priceField.getText());
                    int stock = Integer.parseInt(stockField.getText());
                    return new Product(selectedProduct.getId(), selectedProduct.getName(), price, stock);
                } catch (NumberFormatException e) {
                    showAlert("Błąd", "Nieprawidłowy format liczby");
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();

        result.ifPresent(editedProduct -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE products SET price = ?, stock = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setDouble(1, editedProduct.getPrice());
                stmt.setInt(2, editedProduct.getStock());
                stmt.setInt(3, editedProduct.getId());

                if (stmt.executeUpdate() > 0) {
                    // Aktualizuj lokalnie
                    selectedProduct.setPrice(editedProduct.getPrice());
                    selectedProduct.setStock(editedProduct.getStock());
                    productsTable.refresh();
                    showAlert("Sukces", "Produkt został zaktualizowany.");
                }
            } catch (SQLException e) {
                showAlert("Błąd", "Nie udało się zaktualizować produktu: " + e.getMessage());
            }
        });
    }

    // Klasy modelowe
    /**
     * Klasa reprezentująca użytkownika systemu siłowni.
     * Przechowuje informacje o użytkowniku, takie jak identyfikator, imię i nazwisko,
     * email, hasło, rola i data rejestracji.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class User {
        /**
         * Unikalny identyfikator użytkownika.
         * Przechowywany jako właściwość liczbowa JavaFX dla automatycznej aktualizacji UI.
         */
        private final SimpleIntegerProperty id;

        /**
         * Imię i nazwisko użytkownika.
         * Przechowywane jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty name;

        /**
         * Adres email użytkownika.
         * Przechowywany jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty email;

        /**
         * Rola użytkownika w systemie (np. "admin", "trainer", "client").
         * Przechowywana jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty role;

        private final SimpleBooleanProperty isActive; // Nowa właściwość


        /**
         * Tworzy nowego użytkownika z podanymi parametrami.
         *
         * @param id Unikalny identyfikator użytkownika
         * @param name Imię i nazwisko użytkownika
         * @param email Adres email użytkownika
         * @param role Rola użytkownika w systemie
         */
        public User(int id, String name, String email, String role, boolean isActive) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.role = new SimpleStringProperty(role);
            this.isActive = new SimpleBooleanProperty(isActive);

        }

        // Gettery
        /**
         * Zwraca identyfikator użytkownika.
         * @return Identyfikator użytkownika
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca imię i nazwisko użytkownika.
         * @return Imię i nazwisko użytkownika
         */
        public String getName() { return name.get(); }

        /**
         * Zwraca adres email użytkownika.
         * @return Adres email użytkownika
         */
        public String getEmail() { return email.get(); }

        /**
         * Zwraca rolę użytkownika w systemie.
         * @return Rola użytkownika
         */
        public String getRole() { return role.get(); }

        public boolean getIsActive() { return isActive.get(); }

        // Settery
        /**
         * Ustawia identyfikator użytkownika.
         * @param value Nowy identyfikator użytkownika
         */
        public void setId(int value) {
            id.set(value);
        }

        /**
         * Ustawia imię i nazwisko użytkownika.
         * @param value Nowe imię i nazwisko użytkownika
         */
        public void setName(String value) { name.set(value); }

        /**
         * Ustawia adres email użytkownika.
         * @param value Nowy adres email użytkownika
         */
        public void setEmail(String value) { email.set(value); }

        /**
         * Ustawia rolę użytkownika w systemie.
         * @param value Nowa rola użytkownika
         */
        public void setRole(String value) { role.set(value); }

        // Metody dostępu do właściwości
        /**
         * Zwraca właściwość przechowującą imię i nazwisko użytkownika.
         * @return Właściwość imienia i nazwiska
         */
        public SimpleStringProperty nameProperty() { return name; }

        /**
         * Zwraca właściwość przechowującą adres email użytkownika.
         * @return Właściwość adresu email
         */
        public SimpleStringProperty emailProperty() { return email; }

        /**
         * Zwraca właściwość przechowującą rolę użytkownika.
         * @return Właściwość roli
         */
        public SimpleStringProperty roleProperty() { return role; }
        public void setIsActive(boolean value) { isActive.set(value); }

        public SimpleBooleanProperty isActiveProperty() { return isActive; }

    }

    /**
     * Klasa reprezentująca trening w siłowni.
     * Przechowuje informacje o treningu, takie jak identyfikator, nazwa,
     * typ, trener prowadzący, data, godzina rozpoczęcia, czas trwania i maksymalna liczba uczestników.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class Training {
        /**
         * Unikalny identyfikator treningu.
         * Przechowywany jako właściwość liczbowa JavaFX dla automatycznej aktualizacji UI.
         */
        private final SimpleIntegerProperty id;

        /**
         * Data treningu w formacie tekstowym.
         * Przechowywana jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty trainingDate;

        /**
         * Notatki dotyczące treningu.
         * Przechowywane jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty notes;

        /**
         * Imię i nazwisko klienta uczestniczącego w treningu.
         * Przechowywane jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty clientName;

        /**
         * Imię i nazwisko trenera prowadzącego trening.
         * Przechowywane jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty trainerName;

        /**
         * Tworzy nowy trening z podanymi parametrami.
         *
         * @param id Unikalny identyfikator treningu
         * @param trainingDate Data treningu jako tekst
         * @param notes Notatki dotyczące treningu
         * @param clientName Imię i nazwisko klienta
         * @param trainerName Imię i nazwisko trenera
         */
        public Training(int id, String trainingDate, String notes, String clientName, String trainerName) {
            this.id = new SimpleIntegerProperty(id);
            this.trainingDate = new SimpleStringProperty(trainingDate);
            this.notes = new SimpleStringProperty(notes);
            this.clientName = new SimpleStringProperty(clientName);
            this.trainerName = new SimpleStringProperty(trainerName);
        }

        /**
         * Zwraca identyfikator treningu.
         * @return Identyfikator treningu
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca datę treningu w formacie tekstowym.
         * @return Data treningu
         */
        public String getTrainingDate() { return trainingDate.get(); }

        /**
         * Zwraca notatki dotyczące treningu.
         * @return Notatki treningu
         */
        public String getNotes() { return notes.get(); }

        /**
         * Zwraca imię i nazwisko klienta uczestniczącego w treningu.
         * @return Imię i nazwisko klienta
         */
        public String getClientName() { return clientName.get(); }

        /**
         * Zwraca imię i nazwisko trenera prowadzącego trening.
         * @return Imię i nazwisko trenera
         */
        public String getTrainerName() { return trainerName.get(); }

        public void setTrainingDate(String trainingDate) {
            this.trainingDate.set(trainingDate);
        }

        public void setNotes(String notes) {
            this.notes.set(notes);
        }

        public void setTrainerName(String trainerName) {
            this.trainerName.set(trainerName);
        }
    }

    /**
     * Klasa reprezentująca produkt dostępny w siłowni.
     * Przechowuje informacje o produkcie, takie jak identyfikator, nazwa,
     * kategoria, cena, stan magazynowy i opis.
     * Wykorzystuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class Product {
        /**
         * Unikalny identyfikator produktu.
         * Przechowywany jako właściwość liczbowa JavaFX dla automatycznej aktualizacji UI.
         */
        private final SimpleIntegerProperty id;

        /**
         * Nazwa produktu.
         * Przechowywana jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty name;

        /**
         * Cena produktu.
         * Przechowywana jako właściwość liczbowa zmiennoprzecinkowa JavaFX.
         */
        private final SimpleDoubleProperty price;

        /**
         * Stan magazynowy produktu.
         * Przechowywany jako właściwość liczbowa JavaFX.
         */
        private final SimpleIntegerProperty stock;

        /**
         * Tworzy nowy produkt z podanymi parametrami.
         *
         * @param id Unikalny identyfikator produktu
         * @param name Nazwa produktu
         * @param price Cena produktu
         * @param stock Stan magazynowy produktu
         */
        public Product(int id, String name, double price, int stock) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.stock = new SimpleIntegerProperty(stock);
        }

        // Gettery
        /**
         * Zwraca identyfikator produktu.
         * @return Identyfikator produktu
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca nazwę produktu.
         * @return Nazwa produktu
         */
        public String getName() { return name.get(); }

        /**
         * Zwraca cenę produktu.
         * @return Cena produktu
         */
        public double getPrice() { return price.get(); }

        /**
         * Zwraca stan magazynowy produktu.
         * @return Stan magazynowy
         */
        public int getStock() { return stock.get(); }

        // Settery
        /**
         * Ustawia identyfikator produktu.
         * @param value Nowy identyfikator produktu
         */
        public void setId(int value) { id.set(value); }

        /**
         * Ustawia nazwę produktu.
         * @param value Nowa nazwa produktu
         */
        public void setName(String value) { name.set(value); }

        /**
         * Ustawia cenę produktu.
         * @param value Nowa cena produktu
         */
        public void setPrice(double value) { price.set(value); }

        /**
         * Ustawia stan magazynowy produktu.
         * @param value Nowy stan magazynowy
         */
        public void setStock(int value) { stock.set(value); }

        // Metody dostępowe dla właściwości
        /**
         * Zwraca właściwość identyfikatora produktu.
         * @return Właściwość identyfikatora
         */
        public SimpleIntegerProperty idProperty() { return id; }

        /**
         * Zwraca właściwość nazwy produktu.
         * @return Właściwość nazwy
         */
        public SimpleStringProperty nameProperty() { return name; }

        /**
         * Zwraca właściwość ceny produktu.
         * @return Właściwość ceny
         */
        public SimpleDoubleProperty priceProperty() { return price; }

        /**
         * Zwraca właściwość stanu magazynowego produktu.
         * @return Właściwość stanu magazynowego
         */
        public SimpleIntegerProperty stockProperty() { return stock; }
    }

    /**
     * Klasa reprezentująca karnet członkowski w siłowni.
     * Przechowuje informacje o karnecie, takie jak identyfikator, typ,
     * cena, data rozpoczęcia, data zakończenia i status.
     * Wykorzystuje model JavaFX Property dla automatycznej aktualizacji UI.
     */
    public static class Membership {
        /**
         * Unikalny identyfikator członkostwa/karnetu.
         * Przechowywany jako właściwość liczbowa JavaFX dla automatycznej aktualizacji UI.
         */
        private final SimpleIntegerProperty id;

        /**
         * Imię i nazwisko klienta posiadającego karnet.
         * Przechowywane jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty clientName;

        /**
         * Kwota opłaty za karnet.
         * Przechowywana jako właściwość liczbowa zmiennoprzecinkowa JavaFX.
         */
        private final SimpleDoubleProperty amount;

        /**
         * Data dokonania płatności za karnet w formacie tekstowym.
         * Przechowywana jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty paymentDate;

        /**
         * Tworzy nowy karnet członkowski z podanymi parametrami.
         *
         * @param id Unikalny identyfikator karnetu
         * @param clientName Imię i nazwisko klienta
         * @param amount Kwota opłaty za karnet
         * @param paymentDate Data dokonania płatności jako tekst
         */
        public Membership(int id, String clientName, double amount, String paymentDate) {
            this.id = new SimpleIntegerProperty(id);
            this.clientName = new SimpleStringProperty(clientName);
            this.amount = new SimpleDoubleProperty(amount);
            this.paymentDate = new SimpleStringProperty(paymentDate);
        }

        // Gettery
        /**
         * Zwraca identyfikator karnetu.
         * @return Identyfikator karnetu
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca imię i nazwisko klienta posiadającego karnet.
         * @return Imię i nazwisko klienta
         */
        public String getClientName() { return clientName.get(); }

        /**
         * Zwraca kwotę opłaty za karnet.
         * @return Kwota opłaty
         */
        public double getAmount() { return amount.get(); }

        /**
         * Zwraca datę dokonania płatności za karnet w formacie tekstowym.
         * @return Data płatności
         */
        public String getPaymentDate() { return paymentDate.get(); }

        public void setAmount(double amount) {
            this.amount.set(amount);
        }

        public void setPaymentDate(String paymentDate) {
            this.paymentDate.set(paymentDate);
        }
    }

    /**
     * Klasa reprezentująca transakcję w systemie siłowni.
     * Przechowuje informacje o transakcji, takie jak identyfikator, typ,
     * kwota, data, klient i opis.
     * Implementuje model JavaFX Property dla łatwej integracji z interfejsem użytkownika.
     */
    public static class Transaction {
        /**
         * Unikalny identyfikator transakcji.
         * Przechowywany jako właściwość liczbowa JavaFX dla automatycznej aktualizacji UI.
         */
        private final SimpleIntegerProperty id;

        /**
         * Imię i nazwisko klienta dokonującego transakcji.
         * Przechowywane jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty clientName;

        /**
         * Nazwa produktu, którego dotyczy transakcja.
         * Przechowywana jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty productName;

        /**
         * Data wykonania transakcji w formacie tekstowym.
         * Przechowywana jako właściwość tekstowa JavaFX.
         */
        private final SimpleStringProperty transactionDate;

        /**
         * Kwota transakcji.
         * Przechowywana jako właściwość liczbowa zmiennoprzecinkowa JavaFX.
         */
        private final SimpleDoubleProperty amount;

        /**
         * Tworzy nową transakcję z podanymi parametrami.
         *
         * @param id Unikalny identyfikator transakcji
         * @param clientName Imię i nazwisko klienta
         * @param productName Nazwa produktu
         * @param transactionDate Data transakcji jako tekst
         * @param amount Kwota transakcji
         */
        public Transaction(int id, String clientName, String productName, String transactionDate, double amount) {
            this.id = new SimpleIntegerProperty(id);
            this.clientName = new SimpleStringProperty(clientName);
            this.productName = new SimpleStringProperty(productName);
            this.transactionDate = new SimpleStringProperty(transactionDate);
            this.amount = new SimpleDoubleProperty(amount);
        }

        // Gettery
        /**
         * Zwraca identyfikator transakcji.
         * @return Identyfikator transakcji
         */
        public int getId() { return id.get(); }

        /**
         * Zwraca imię i nazwisko klienta dokonującego transakcji.
         * @return Imię i nazwisko klienta
         */
        public String getClientName() { return clientName.get(); }

        /**
         * Zwraca nazwę produktu, którego dotyczy transakcja.
         * @return Nazwa produktu
         */
        public String getProductName() { return productName.get(); }

        /**
         * Zwraca datę wykonania transakcji w formacie tekstowym.
         * @return Data transakcji
         */
        public String getTransactionDate() { return transactionDate.get(); }

        /**
         * Zwraca kwotę transakcji.
         * @return Kwota transakcji
         */
        public double getAmount() { return amount.get(); }

        public void setClientName(String value) { clientName.set(value); }
        public void setAmount(double value) { amount.set(value); }
        public void setTransactionDate(String value) { transactionDate.set(value); }
    }

    public static class TransactionDataImpl implements TransactionData {
        private final int id;
        private final String clientName;
        private final String productName;
        private final String date;
        private final double amount;

        public TransactionDataImpl(int id, String clientName, String productName, String date, double amount) {
            this.id = id;
            this.clientName = clientName;
            this.productName = productName;
            this.date = date;
            this.amount = amount;
        }

        @Override public int getId() { return id; }
        @Override public String getDisplayName() { return clientName + " - " + productName; }
        @Override public String getClientName() { return clientName; }
        @Override public String getProductName() { return productName; }
        @Override public String getDate() { return date; }
        @Override public double getAmount() { return amount; }
    }

    public static class MembershipDataImpl implements MembershipData {
        private final int id;
        private final String clientName;
        private final double amount;
        private final String date;

        public MembershipDataImpl(int id, String clientName, double amount, String date) {
            this.id = id;
            this.clientName = clientName;
            this.amount = amount;
            this.date = date;
        }

        @Override public int getId() { return id; }
        @Override public String getDisplayName() { return clientName; }
        @Override public String getClientName() { return clientName; }
        @Override public double getAmount() { return amount; }
        @Override public String getDate() { return date; }
    }

    public static class ProductDataImpl implements ProductData {
        private final int id;
        private final String name;
        private final double price;
        private final int stock;

        public ProductDataImpl(int id, String name, double price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        @Override public int getId() { return id; }
        @Override public String getDisplayName() { return name; }
        @Override public String getName() { return name; }
        @Override public double getPrice() { return price; }
        @Override public int getStock() { return stock; }
    }

}