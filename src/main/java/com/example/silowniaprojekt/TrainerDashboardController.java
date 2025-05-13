package com.example.silowniaprojekt;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.util.Optional;

public class TrainerDashboardController {

    // Sekcje interfejsu
    @FXML private VBox dashboardSection;
    @FXML private VBox trainingPlansSection;
    @FXML private VBox goalsSection;
    @FXML private VBox progressSection;
    @FXML private VBox scheduleSection;
    @FXML private VBox clientsSection;
    @FXML private VBox incomeSection;
    @FXML private VBox attendanceSection;

    // Tabele
    @FXML private TableView<IncomeData> incomeTable;
    @FXML private TableColumn<IncomeData, String> dateColumn;
    @FXML private TableColumn<IncomeData, String> clientColumn;
    @FXML private TableColumn<IncomeData, String> trainingColumn;
    @FXML private TableColumn<IncomeData, Double> amountColumn;

    @FXML private TableView<AttendanceData> attendanceTable;
    @FXML private TableColumn<AttendanceData, String> attDateColumn;
    @FXML private TableColumn<AttendanceData, String> attTrainingColumn;
    @FXML private TableColumn<AttendanceData, String> attClientColumn;
    @FXML private TableColumn<AttendanceData, String> attStatusColumn;

    @FXML private ComboBox<String> clientComboBox;
    @FXML private VBox exercisesContainer;

    @FXML private ComboBox<String> goalsClientCombo;
    @FXML private VBox goalsContainer;

    @FXML private TableView<ScheduleData> scheduleTable;
    @FXML private TableColumn<ScheduleData, String> timeColumn;
    @FXML private TableColumn<ScheduleData, String> mondayColumn;
    @FXML private TableColumn<ScheduleData, String> tuesdayColumn;
    @FXML private TableColumn<ScheduleData, String> wednesdayColumn;
    @FXML private TableColumn<ScheduleData, String> thursdayColumn;
    @FXML private TableColumn<ScheduleData, String> fridayColumn;
    @FXML private TableColumn<ScheduleData, String> saturdayColumn;

    @FXML private VBox clientsContainer;
    private ObservableList<Client> clients = FXCollections.observableArrayList();

    @FXML
    private Label statusLabel;



    @FXML
    public void initialize() {
        configureTables();
        loadSampleData();
        showDashboard();
        configureProgressSection();
        configureGoalsSection();
        configureScheduleTable();
        initializeSampleClients();
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            statusLabel.setText("Połączenie z bazą danych nawiązane");
            try {
                conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            statusLabel.setText("Błąd połączenia z bazą danych");
        }
    }

    private void initializeSampleClients() {
        clients.addAll(
                new Client("Jan Kowalski", "jan@example.com", "pass123", "client"),
                new Client("Anna Nowak", "anna@example.com", "pass456", "client")
        );
        refreshClientsList();
    }
    @FXML
    private void handleAddClient() {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Dodaj nowego klienta");
        dialog.setHeaderText("Wprowadź dane nowego klienta");

        ButtonType addButtonType = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField fullNameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();

        grid.add(new Label("Imię i nazwisko:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Hasło:"), 0, 2);
        grid.add(passwordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Client(
                        fullNameField.getText(),
                        emailField.getText(),
                        passwordField.getText(),
                        "client"
                );
            }
            return null;
        });

        Optional<Client> result = dialog.showAndWait();
        result.ifPresent(client -> {
            clients.add(client);
            refreshClientsList();
        });
    }

    private void refreshClientsList() {
        clientsContainer.getChildren().clear();

        for (Client client : clients) {
            VBox card = new VBox(10);
            card.getStyleClass().add("client-card");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(client.getFullName());
            nameLabel.getStyleClass().add("client-name");

            Label statusLabel = new Label("Nowy klient");
            statusLabel.getStyleClass().add("client-status-new");

            ProgressBar progressBar = new ProgressBar(0.0);
            progressBar.getStyleClass().add("client-progress");

            HBox buttons = new HBox(10);
            Button editBtn = new Button("Edytuj");
            Button deleteBtn = new Button("Usuń");
            editBtn.getStyleClass().add("client-action-btn");
            deleteBtn.getStyleClass().add("client-action-btn");

            header.getChildren().addAll(nameLabel, statusLabel);
            buttons.getChildren().addAll(editBtn, deleteBtn);
            card.getChildren().addAll(header, progressBar, buttons);

            clientsContainer.getChildren().add(card);
        }
    }
    private void configureScheduleTable() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        mondayColumn.setCellValueFactory(new PropertyValueFactory<>("monday"));
        tuesdayColumn.setCellValueFactory(new PropertyValueFactory<>("tuesday"));
        wednesdayColumn.setCellValueFactory(new PropertyValueFactory<>("wednesday"));
        thursdayColumn.setCellValueFactory(new PropertyValueFactory<>("thursday"));
        fridayColumn.setCellValueFactory(new PropertyValueFactory<>("friday"));
        saturdayColumn.setCellValueFactory(new PropertyValueFactory<>("saturday"));

        // Załaduj przykładowe dane
        ObservableList<ScheduleData> scheduleData = FXCollections.observableArrayList(
                new ScheduleData(
                        "07:00-08:00",
                        "Przysiady\nJan Nowak\n5 serii\n12x20kg 10x22kg",
                        "Dlospa\nJan Nowak\n3 serie\n12x20kg 10x22kg 8x24kg",
                        "Bieganie\n10 minut\nDystans 5km",
                        "Sumo Spisu\n5 serii\n15x40kg",
                        "Chust Fly\n3 serie\n12x15kg",
                        "Jumping Jacks\n30 powtórzeń"
                )
        );
        scheduleTable.setItems(scheduleData);
    }

    private void setMultilineCellFactory(TableColumn<ScheduleData, String> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            private final Text text = new Text();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item.replace("\\n", "\n"));
                    text.setWrappingWidth(column.getWidth() - 10);
                    setGraphic(text);
                }
            }
        });
    }
    private void configureGoalsSection() {
        // Lista klientów
        goalsClientCombo.setItems(FXCollections.observableArrayList(
                "Jan Kowalski",
                "Anna Nowak",
                "Marek Wiśniewski"
        ));

        // Listener dla wyboru klienta
        goalsClientCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> loadClientGoals(newVal)
        );
    }

    private void loadClientGoals(String client) {
        goalsContainer.getChildren().clear();

        // Przykładowe dane
        ObservableList<GoalData> goals = FXCollections.observableArrayList(
                new GoalData("Przysiady ze sztangą", "120 kg"),
                new GoalData("Martwy ciąg", "180 kg"),
                new GoalData("Wyciskanie na ławce", "100 kg")
        );

        // Dynamiczne generowanie kart
        for (GoalData goal : goals) {
            VBox card = new VBox();
            card.getStyleClass().add("goal-card");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label title = new Label();
            title.textProperty().bind(goal.exerciseProperty());
            title.getStyleClass().add("goal-title");

            Label target = new Label();
            target.textProperty().bind(goal.targetProperty());
            target.getStyleClass().add("goal-target");

            HBox buttons = new HBox(10);
            Button editBtn = new Button("Edytuj");
            Button deleteBtn = new Button("Usuń");
            editBtn.getStyleClass().add("goal-btn");
            deleteBtn.getStyleClass().add("goal-btn");

            header.getChildren().addAll(title, target);
            buttons.getChildren().addAll(editBtn, deleteBtn);
            card.getChildren().addAll(header, buttons);
            goalsContainer.getChildren().add(card);
        }
    }
    private void configureProgressSection() {
        // Lista klientów
        clientComboBox.setItems(FXCollections.observableArrayList(
                "Jan Kowalski",
                "Anna Nowak",
                "Marek Wiśniewski"
        ));

        // Listener dla wyboru klienta
        clientComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> loadClientProgress(newVal)
        );
    }

    private void loadClientProgress(String client) {
        exercisesContainer.getChildren().clear();

        // Przykładowe dane - można zastąpić pobieraniem z bazy
        ObservableList<ProgressData> exercises = FXCollections.observableArrayList(
                new ProgressData("Przysiady ze sztangą 120 kg", 0.75, "120 kg"),
                new ProgressData("Martwy ciąg 180 kg", 0.67, "180 kg"),
                new ProgressData("Wyciskanie na ławce 100 kg", 0.80, "100 kg")
        );

        // Dynamiczne generowanie kart ćwiczeń
        for (ProgressData exercise : exercises) {
            VBox card = new VBox();
            card.getStyleClass().add("exercise-card");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label title = new Label();
            title.textProperty().bind(exercise.exerciseNameProperty());
            title.getStyleClass().add("exercise-title");

            Label percent = new Label();
            percent.textProperty().bind(exercise.progressProperty().multiply(100).asString("%.0f%% ukończone"));
            percent.getStyleClass().add("exercise-percent");

            ProgressBar progressBar = new ProgressBar();
            progressBar.progressProperty().bind(exercise.progressProperty());
            progressBar.getStyleClass().add("exercise-progress");

            Button btn = new Button("Aktywuj");
            btn.getStyleClass().add("exercise-btn");

            header.getChildren().addAll(title, percent);
            card.getChildren().addAll(header, progressBar, btn);
            exercisesContainer.getChildren().add(card);
        }
    }

    private void configureTables() {
        // Konfiguracja tabeli dochodów
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        clientColumn.setCellValueFactory(new PropertyValueFactory<>("client"));
        trainingColumn.setCellValueFactory(new PropertyValueFactory<>("training"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        incomeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Konfiguracja tabeli frekwencji
        attDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        attTrainingColumn.setCellValueFactory(new PropertyValueFactory<>("training"));
        attClientColumn.setCellValueFactory(new PropertyValueFactory<>("client"));
        attStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        attendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Kolorowanie statusów
        attStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals("Obecny") ?
                            "-fx-text-fill: #00ff00;" :
                            "-fx-text-fill: #ff0000;");
                }
            }
        });
    }

    private void loadSampleData() {
        // Przykładowe dane dochodów
        ObservableList<IncomeData> incomeData = FXCollections.observableArrayList(
                new IncomeData("2024-03-20", "Jan Kowalski", "CrossFit", 150.0),
                new IncomeData("2024-03-19", "Anna Nowak", "Joga", 100.0),
                new IncomeData("2024-03-18", "Marek Wiśniewski", "TRX", 120.0)
        );
        incomeTable.setItems(incomeData);

        // Przykładowe dane frekwencji
        ObservableList<AttendanceData> attendanceData = FXCollections.observableArrayList(
                new AttendanceData("2024-03-20", "CrossFit", "Jan Kowalski", "Obecny"),
                new AttendanceData("2024-03-20", "CrossFit", "Anna Nowak", "Nieobecna"),
                new AttendanceData("2024-03-19", "Joga", "Marek Wiśniewski", "Obecny")
        );
        attendanceTable.setItems(attendanceData);
    }

    private void hideAllSections() {
        dashboardSection.setVisible(false);
        trainingPlansSection.setVisible(false);
        goalsSection.setVisible(false);
        progressSection.setVisible(false);
        scheduleSection.setVisible(false);
        clientsSection.setVisible(false);
        incomeSection.setVisible(false);
        attendanceSection.setVisible(false);
    }

    // Metody przełączania widoków
    @FXML private void showDashboard() { toggleSection(dashboardSection); }
    @FXML private void showTrainingPlans() { toggleSection(trainingPlansSection); }
    @FXML private void showGoals() { toggleSection(goalsSection); }
    @FXML private void showProgress() { toggleSection(progressSection); }
    @FXML private void showSchedule() { toggleSection(scheduleSection); }
    @FXML private void showClients() { toggleSection(clientsSection); }
    @FXML private void showIncome() { toggleSection(incomeSection); }
    @FXML private void showAttendance() { toggleSection(attendanceSection); }

    private void toggleSection(VBox section) {
        hideAllSections();
        section.setVisible(true);
        section.setManaged(true);
    }

}