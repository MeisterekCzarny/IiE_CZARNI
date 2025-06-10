import com.example.silowniaprojekt.DatabaseConnection;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test sprawdzający, czy można pobrać liczbę produktów z bazy danych.
 * Nie wymaga sztywnych danych, działa na dowolnej liczbie rekordów.
 */
public class ProductCountDatabaseTest {
    @Test
    public void testProductCountQuery() {
        Connection connection = DatabaseConnection.getConnection();
        assertNotNull(connection, "Połączenie nie powinno być null.");
        try (Statement stmt = connection.createStatement()) {
            // Zmień nazwę tabeli na odpowiednią, jeśli nie masz 'products'
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products");
            assertTrue(rs.next(), "Zapytanie powinno zwrócić wynik.");
            int count = rs.getInt(1);
            assertTrue(count >= 0, "Liczba produktów powinna być >= 0");
            System.out.println("Liczba produktów w bazie: " + count);
        } catch (SQLException ex) {
            fail("Wyjątek podczas zapytania do bazy: " + ex.getMessage());
        }
    }
}