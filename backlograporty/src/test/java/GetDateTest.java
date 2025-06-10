import com.example.silowniaprojekt.AdminDashboardController;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GetDateTest {

    @Test
    void testGetDateRangeForPeriod() {
        AdminDashboardController controller = new AdminDashboardController();

        // Test dla "Ostatni miesiąc"
        LocalDate[] lastMonth = controller.getDateRangeForPeriod("Ostatni miesiąc");
        assertEquals(LocalDate.now().minusMonths(1), lastMonth[0]);
        assertEquals(LocalDate.now(), lastMonth[1]);

        // Test dla niestandardowego zakresu dat
        LocalDate[] custom = controller.getDateRangeForPeriod("2023-01-01:2023-12-31");
        assertEquals(LocalDate.of(2023, 1, 1), custom[0]);
        assertEquals(LocalDate.of(2023, 12, 31), custom[1]);
    }
}