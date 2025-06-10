package com.example.silowniaprojekt;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private Stage loginStage; // Referencja do okna logowania

    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Błąd logowania", "Wprowadź zarówno email, jak i hasło");
            return;
        }

        // Statyczne dane testowe
        try {
            if (email.equalsIgnoreCase("admin@example.com") && password.equals("admin123")) {
                loadAdminDashboard(4,email);
                return;
            } else if (email.equalsIgnoreCase("trener@example.com") && password.equals("trener123")) {
                loadTrainerDashboard(2, "Statyczny Trener");
                return;
            } else if (email.equalsIgnoreCase("pracownik@example.com") && password.equals("pracownik123")) {
                loadEmployeeDashboard();
                return;
            } else if (email.equalsIgnoreCase("klient@example.com") && password.equals("klient123")) {
                loadClientDashboard(1, "Jakub Matlosz", email);
                return;
            }
        } catch (IOException e) {
            handleLoadingError(e);
            return;
        }

        // Logowanie z bazą danych
        try {
            authenticateUser(email, password);
        } catch (IOException e) {
            handleLoadingError(e);
        }
    }

    public boolean authenticateUser(String email, String password) throws IOException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showAlert("Błąd połączenia", "Nie można połączyć się z bazą danych");
                return false;
            }

            String query = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                int userId = rs.getInt("id");
                String userName = rs.getString("name");
                String userRole = rs.getString("role");

                if (BCrypt.checkpw(password, storedHash)) {
                    switch (userRole) {
                        case "admin":
                            loadAdminDashboard(userId,userName);
                            break;
                        case "trainer":
                            loadTrainerDashboard(userId, userName);
                            break;
                        case "employee":
                            loadEmployeeDashboard();
                            break;
                        case "client":
                            loadClientDashboard(userId, userName, email);
                            break;
                        default:
                            showAlert("Błąd logowania", "Nieznana rola użytkownika");
                            return false;
                    }

                    return true;
                } else {
                    showAlert("Błąd logowania", "Nieprawidłowe hasło");
                    return false;
                }
            } else {
                showAlert("Błąd logowania", "Nie znaleziono użytkownika");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas logowania: " + e.getMessage());
            showAlert("Błąd logowania", "Wystąpił problem z bazą danych: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Błąd weryfikacji hasła: " + e.getMessage());
            showAlert("Błąd logowania", "Nieprawidłowy format hasła w bazie danych");
            return false;
        }
    }



    public void loadAdminDashboard(int userId, String email) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/silowniaprojekt/admin_dashboard.fxml"));
        Parent root = loader.load();

        AdminDashboardController controller = loader.getController();
        controller.setCurrentUser(userId, email); // ← TO DODAJ

        Stage dashboardStage = new Stage();
        dashboardStage.setTitle("Black Iron Gym - Panel Administratora");
        dashboardStage.setScene(new Scene(root));
        dashboardStage.setMaximized(true);
        dashboardStage.show();

        loginStage.close(); // Zamknięcie okna logowania
    }


    public void loadTrainerDashboard(int trainerId, String trainerName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("trainer_dashboard.fxml"));
        Parent root = loader.load();

        TrainerDashboardController trainerController = loader.getController();
        trainerController.setTrainerData(trainerId, trainerName);

        Stage dashboardStage = new Stage();
        dashboardStage.setTitle("Black Iron Gym - Panel Trenera");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("trainer_styles.css").toExternalForm());
        dashboardStage.setScene(scene);
        dashboardStage.setMaximized(true);
        dashboardStage.show();

        loginStage.close(); // Zamknięcie okna logowania
    }

    public void loadEmployeeDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("employee_dashboard.fxml"));
        Parent root = loader.load();

        Stage dashboardStage = new Stage();
        dashboardStage.setTitle("Black Iron Gym - Panel Pracownika");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("employee_styles.css").toExternalForm());
        dashboardStage.setScene(scene);
        dashboardStage.setMaximized(true);
        dashboardStage.show();

        loginStage.close(); // Zamknięcie okna logowania
    }

    public void loadClientDashboard(int userId, String userName, String userEmail) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client_dashboard.fxml"));
        Parent root = loader.load();

        ClientDashboardController controller = loader.getController();
        controller.setUserData(userId, userName, userEmail);

        Stage dashboardStage = new Stage();
        dashboardStage.setTitle("Black Iron Gym - Panel Klienta");
        dashboardStage.setScene(new Scene(root));
        dashboardStage.setMaximized(true);
        dashboardStage.show();

        loginStage.close(); // Zamknięcie okna logowania
    }

    public void handleLoadingError(Exception e) {
        e.printStackTrace();
        showAlert("Błąd ładowania", "Wystąpił problem podczas ładowania panelu:\n" + e.getMessage());
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

