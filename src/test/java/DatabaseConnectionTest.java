
import com.example.silowniaprojekt.DatabaseConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    public void testGetConnection_validConnection() {
        // Próba nawiązania połączenia do bazy danych
        Connection connection = DatabaseConnection.getConnection();
        assertNotNull(connection, "Połączenie nie powinno być null.");

        try {
            // Sprawdzenie, czy połączenie jest aktywne
            assertFalse(connection.isClosed(), "Połączenie powinno być otwarte.");
            // Zamknięcie połączenia po zakończeniu testu
            connection.close();
        } catch (SQLException ex) {
            fail("Wyjątek podczas sprawdzania stanu połączenia: " + ex.getMessage());
        }
    }
}
