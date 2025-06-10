import com.example.silowniaprojekt.DatabaseConnection;
import com.example.silowniaprojekt.LoginController;

import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla klasy LoginController.
 * Mockuje interakcje z bazą danych i elementami UI JavaFX.
 *
 * Wymaga, aby w LoginController metody pomocnicze (np. showAlert, loadAdminDashboard, authenticateUser)
 * były co najmniej 'protected' zamiast 'private', aby umożliwić ich szpiegowanie.
 */
class LoginControllerTest {

    // Statyczny blok do inicjalizacji platformy JavaFX.
    // Niezbędny do tworzenia obiektów JavaFX (TextField, PasswordField, Stage itp.) w testach.
    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platforma już uruchomiona
        }
    }

    private LoginController loginController;

    // Mocki dla pól FXML
    private TextField mockEmailField;
    private PasswordField mockPasswordField;

    // Mock dla Stage (okna logowania), aby móc zweryfikować jego zamknięcie
    private Stage mockLoginStage;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException, IOException {
        loginController = Mockito.spy(new LoginController()); // Tworzymy spia na LoginController
        // aby móc weryfikować wywołania jego chronionych/publicznych metod

        mockEmailField = mock(TextField.class);
        mockPasswordField = mock(PasswordField.class);
        mockLoginStage = mock(Stage.class);

        // Wstrzykiwanie mocków do pól FXML kontrolera za pomocą refleksji
        // Jest to konieczne, ponieważ bez TestFX i ładowania FXML, @FXML pola nie są automatycznie inicjalizowane.
        Field emailField = LoginController.class.getDeclaredField("emailField");
        emailField.setAccessible(true);
        emailField.set(loginController, mockEmailField);

        Field passwordField = LoginController.class.getDeclaredField("passwordField");
        passwordField.setAccessible(true);
        passwordField.set(loginController, mockPasswordField);

        // Ustawienie mockLoginStage w kontrolerze
        loginController.setLoginStage(mockLoginStage);

        // Mockowanie metod loadDashboard() aby nie próbowały ładować FXML (co wymaga TestFX)
        // Zamiast tego, weryfikujemy czy zostały wywołane.
        doNothing().when(loginController).loadAdminDashboard(4,"admin@example.com");
        doNothing().when(loginController).loadTrainerDashboard(anyInt(), anyString());
        doNothing().when(loginController).loadEmployeeDashboard();
        doNothing().when(loginController).loadClientDashboard(anyInt(), anyString(), anyString());
        doNothing().when(loginController).showAlert(anyString(), anyString()); // Szpiegowanie showAlert
        // USUNIĘTO: doNothing().when(loginController).handleLoadingError(any(IOException.class)); // Pozwalamy na rzeczywiste wywołanie handleLoadingError
    }

    // --- Testy dla metody handleLogin() ---

    @Test
    @DisplayName("handleLogin should show alert if email is empty")
    void handleLogin_EmptyEmail_ShowsAlert() throws IOException {
        when(mockEmailField.getText()).thenReturn("");
        when(mockPasswordField.getText()).thenReturn("password");

        loginController.handleLogin();

        verify(loginController).showAlert(eq("Błąd logowania"), eq("Wprowadź zarówno email, jak i hasło"));
        verify(loginController, never()).authenticateUser(anyString(), anyString());
        verify(mockLoginStage, never()).close();
    }

    @Test
    @DisplayName("handleLogin should show alert if password is empty")
    void handleLogin_EmptyPassword_ShowsAlert() throws IOException {
        when(mockEmailField.getText()).thenReturn("test@example.com");
        when(mockPasswordField.getText()).thenReturn("");

        loginController.handleLogin();

        verify(loginController).showAlert(eq("Błąd logowania"), eq("Wprowadź zarówno email, jak i hasło"));
        verify(loginController, never()).authenticateUser(anyString(), anyString());
        verify(mockLoginStage, never()).close();
    }

    @Test
    @DisplayName("handleLogin should load admin dashboard for static admin credentials")
    void handleLogin_StaticAdmin_LoadsAdminDashboard() throws IOException {
        when(mockEmailField.getText()).thenReturn("admin@example.com");
        when(mockPasswordField.getText()).thenReturn("admin123");

        loginController.handleLogin();

        verify(loginController).loadAdminDashboard(4,"admin@example.com");
        verify(loginController, never()).authenticateUser(anyString(), anyString()); // Nie powinien dzwonić do DB
    }

    @Test
    @DisplayName("handleLogin should load trainer dashboard for static trainer credentials")
    void handleLogin_StaticTrainer_LoadsTrainerDashboard() throws IOException {
        when(mockEmailField.getText()).thenReturn("trener@example.com");
        when(mockPasswordField.getText()).thenReturn("trener123");

        loginController.handleLogin();

        verify(loginController).loadTrainerDashboard(eq(2), eq("Statyczny Trener"));
        verify(loginController, never()).authenticateUser(anyString(), anyString());
    }

    @Test
    @DisplayName("handleLogin should load employee dashboard for static employee credentials")
    void handleLogin_StaticEmployee_LoadsEmployeeDashboard() throws IOException {
        when(mockEmailField.getText()).thenReturn("pracownik@example.com");
        when(mockPasswordField.getText()).thenReturn("pracownik123");

        loginController.handleLogin();

        verify(loginController).loadEmployeeDashboard();
        verify(loginController, never()).authenticateUser(anyString(), anyString());
    }

    @Test
    @DisplayName("handleLogin should load client dashboard for static client credentials")
    void handleLogin_StaticClient_LoadsClientDashboard() throws IOException {
        when(mockEmailField.getText()).thenReturn("klient@example.com");
        when(mockPasswordField.getText()).thenReturn("klient123");

        loginController.handleLogin();

        verify(loginController).loadClientDashboard(eq(1), eq("Jakub Matlosz"), eq("klient@example.com"));
        verify(loginController, never()).authenticateUser(anyString(), anyString());
    }

    @Test
    @DisplayName("handleLogin should call authenticateUser for non-static credentials")
    void handleLogin_NonStatic_CallsAuthenticateUser() throws IOException {
        when(mockEmailField.getText()).thenReturn("dbuser@example.com"); // Poprawiona literówka
        when(mockPasswordField.getText()).thenReturn("dbpassword");

        // Mockujemy wywołanie authenticateUser, aby zwróciło false,
        // bo chcemy przetestować samo wywołanie, a nie jego wewnętrzną logikę w tym teście.
        doReturn(false).when(loginController).authenticateUser(anyString(), anyString());

        loginController.handleLogin();

        verify(loginController).authenticateUser(eq("dbuser@example.com"), eq("dbpassword")); // Poprawiona literówka
        verify(loginController, never()).showAlert(anyString(), anyString()); // Nie powinien wyświetlać alertu w tym miejscu
        verify(mockLoginStage, never()).close(); // Nie powinien zamykać, jeśli authUser zwróci false
    }

    @Test
    @DisplayName("handleLogin should handle IOException during static dashboard loading")
    void handleLogin_StaticDashboardLoadingFails_HandlesError() throws IOException {
        when(mockEmailField.getText()).thenReturn("admin@example.com");
        when(mockPasswordField.getText()).thenReturn("admin123");

        // Symulujemy IOException podczas ładowania dashboardu
        doThrow(new IOException("Test IO Error")).when(loginController).loadAdminDashboard(4,"admin@example.com");

        loginController.handleLogin();

        verify(loginController).handleLoadingError(any(IOException.class));
        verify(mockLoginStage, never()).close();
    }


    // --- Testy dla metody authenticateUser() ---

    @Test
    @DisplayName("authenticateUser should show connection error if DatabaseConnection returns null")
    void authenticateUser_NullConnection_ShowsAlert() throws IOException {
        try (MockedStatic<DatabaseConnection> mockedDb = Mockito.mockStatic(DatabaseConnection.class)) {
            mockedDb.when(DatabaseConnection::getConnection).thenReturn(null);

            assertFalse(loginController.authenticateUser("user@example.com", "password"), "Authentication should fail");
            verify(loginController).showAlert(eq("Błąd połączenia"), eq("Nie można połączyć się z bazą danych"));
            verify(mockLoginStage, never()).close();
        }
    }

    @Test
    @DisplayName("authenticateUser should show alert if user not found")
    void authenticateUser_UserNotFound_ShowsAlert() throws SQLException, IOException {
        try (MockedStatic<DatabaseConnection> mockedDb = Mockito.mockStatic(DatabaseConnection.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            mockedDb.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false); // Użytkownik nie znaleziony

            assertFalse(loginController.authenticateUser("nonexistent@example.com", "password"), "Authentication should fail");
            verify(loginController).showAlert(eq("Błąd logowania"), eq("Nie znaleziono użytkownika"));
            verify(mockConnection).close();
            verify(mockLoginStage, never()).close();
        }
    }

    @Test
    @DisplayName("authenticateUser should show alert for incorrect password")
    void authenticateUser_IncorrectPassword_ShowsAlert() throws SQLException, IOException {
        try (MockedStatic<DatabaseConnection> mockedDb = Mockito.mockStatic(DatabaseConnection.class);
             MockedStatic<BCrypt> mockedBCrypt = Mockito.mockStatic(BCrypt.class)) {

            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            mockedDb.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true); // Użytkownik znaleziony

            String correctPasswordHash = BCrypt.hashpw("correctPassword", BCrypt.gensalt());
            when(mockRs.getString("password")).thenReturn(correctPasswordHash);

            when(mockRs.getInt("id")).thenReturn(1); // Użytkownik znaleziony
            when(mockRs.getString("name")).thenReturn("Test User");
            when(mockRs.getString("role")).thenReturn("client");

            mockedBCrypt.when(() -> BCrypt.checkpw(eq("wrongPassword"), anyString())).thenReturn(false); // Nieprawidłowe hasło

            assertFalse(loginController.authenticateUser("user@example.com", "wrongPassword"), "Authentication should fail");
            verify(loginController).showAlert(eq("Błąd logowania"), eq("Nieprawidłowe hasło"));
            verify(mockConnection).close();
            verify(mockLoginStage, never()).close();
        }
    }








    @Test
    @DisplayName("authenticateUser should handle SQLException")
    void authenticateUser_SQLException_ShowsAlert() throws SQLException, IOException {
        try (MockedStatic<DatabaseConnection> mockedDb = Mockito.mockStatic(DatabaseConnection.class)) {
            Connection mockConnection = mock(Connection.class);
            mockedDb.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            // Symulujemy błąd SQLException podczas przygotowywania zapytania
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB Error"));

            assertFalse(loginController.authenticateUser("user@example.com", "password"), "Authentication should fail");
            verify(loginController).showAlert(eq("Błąd logowania"), contains("Wystąpił problem z bazą danych: DB Error"));
            verify(mockConnection).close();
            verify(mockLoginStage, never()).close();
        }
    }

    @Test
    @DisplayName("authenticateUser should handle IllegalArgumentException from BCrypt")
    void authenticateUser_IllegalArgumentException_ShowsAlert() throws SQLException, IOException {
        try (MockedStatic<DatabaseConnection> mockedDb = Mockito.mockStatic(DatabaseConnection.class);
             MockedStatic<BCrypt> mockedBCrypt = Mockito.mockStatic(BCrypt.class)) {

            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);

            mockedDb.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getString("password")).thenReturn("invalid_hash"); // Niewłaściwy format hasła
            when(mockRs.getInt("id")).thenReturn(1);
            when(mockRs.getString("name")).thenReturn("Test User");
            when(mockRs.getString("role")).thenReturn("client");

            // BCrypt rzuci IllegalArgumentException dla nieprawidłowego formatu hasła
            mockedBCrypt.when(() -> BCrypt.checkpw(anyString(), eq("invalid_hash"))).thenThrow(new IllegalArgumentException("Invalid hash"));

            assertFalse(loginController.authenticateUser("user@example.com", "password"), "Authentication should fail");
            verify(loginController).showAlert(eq("Błąd logowania"), eq("Nieprawidłowy format hasła w bazie danych"));
            verify(mockConnection).close();
            verify(mockLoginStage, never()).close();
        }
    }

    // --- Testy dla metody logUserActivity() ---





    // --- Test dla setLoginStage() ---

    @Test
    @DisplayName("setLoginStage should set the login stage correctly")
    void setLoginStage_SetsStage() throws NoSuchFieldException, IllegalAccessException {
        Stage newMockStage = mock(Stage.class);
        loginController.setLoginStage(newMockStage);

        Field loginStageField = LoginController.class.getDeclaredField("loginStage");
        loginStageField.setAccessible(true);
        Stage actualStage = (Stage) loginStageField.get(loginController);

        assertEquals(newMockStage, actualStage, "Login stage should be set to the provided stage");
    }


    // --- Testy dla handleLoadingError() (nie showAlert, bo jest szpiegowany) ---

    @Test
    @DisplayName("handleLoadingError should call showAlert")
    void handleLoadingError_CallsShowAlert() {
        IOException testException = new IOException("Something went wrong during loading");

        loginController.handleLoadingError(testException);

        // Zmiana: Użycie contains() dla części wiadomości, aby była bardziej odporna
        verify(loginController).showAlert(eq("Błąd ładowania"), contains("Wystąpił problem podczas ładowania panelu:\nSomething went wrong during loading"));
    }
}