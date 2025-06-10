import com.example.silowniaprojekt.AdminDashboardController;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdminDashboardControllerTest {

    @Test
    void productConstructor_ShouldSetFieldsCorrectly() {
        AdminDashboardController.Product product =
                new AdminDashboardController.Product(1, "Laptop", 3000.0,5);

        assertEquals(1, product.getId());
        assertEquals("Laptop", product.getName());
        assertEquals(3000.0, product.getPrice());
    }

    @Test
    void productIdProperty_ShouldReflectChanges() {
        var product = new AdminDashboardController.Product(2, "Phone", 2000.0,2);
        product.idProperty().set(5);
        assertEquals(5, product.getId());
    }

    @Test
    void productNameProperty_ShouldReflectChanges() {
        var product = new AdminDashboardController.Product(3, "Tablet", 1000.0,6);
        product.nameProperty().set("Updated Tablet");
        assertEquals("Updated Tablet", product.getName());
    }

    @Test
    void productPriceProperty_ShouldReflectChanges() {
        var product = new AdminDashboardController.Product(4, "Monitor", 500.0,12);
        product.priceProperty().set(750.0);
        assertEquals(750.0, product.getPrice());
    }
        @Test
        void userConstructor_ShouldInitializeFieldsCorrectly() {
            var user = new AdminDashboardController.User(
                    1, "Jan Kowalski", "jan.kowalski@example.com", "admin", true
            );

            assertEquals(1, user.getId());
            assertEquals("Jan Kowalski", user.getName());
            assertEquals("jan.kowalski@example.com", user.getEmail());
            assertEquals("admin", user.getRole());
            assertTrue(user.getIsActive());
        }

        @Test
        void setters_ShouldUpdateUserFields() {
            var user = new AdminDashboardController.User(2, "Anna Nowak", "anna@example.com", "trainer", false);

            user.setId(5);
            user.setName("Anna Kowalska");
            user.setEmail("kowalska@example.com");
            user.setRole("client");
            user.setIsActive(true);

            assertEquals(5, user.getId());
            assertEquals("Anna Kowalska", user.getName());
            assertEquals("kowalska@example.com", user.getEmail());
            assertEquals("client", user.getRole());
            assertTrue(user.getIsActive());
        }

        @Test
        void nameProperty_ShouldReflectChanges() {
            var user = new AdminDashboardController.User(3, "Paweł Zieliński", "pawel@example.com", "client", true);
            user.nameProperty().set("Paweł Z.");
            assertEquals("Paweł Z.", user.getName());
        }

        @Test
        void emailProperty_ShouldReflectChanges() {
            var user = new AdminDashboardController.User(4, "Maria Nowak", "maria@example.com", "trainer", true);
            user.emailProperty().set("nowak@example.com");
            assertEquals("nowak@example.com", user.getEmail());
        }

        @Test
        void roleProperty_ShouldReflectChanges() {
            var user = new AdminDashboardController.User(5, "Tomasz Lis", "tomasz@example.com", "client", false);
            user.roleProperty().set("admin");
            assertEquals("admin", user.getRole());
        }

        @Test
        void isActiveProperty_ShouldReflectChanges() {
            var user = new AdminDashboardController.User(6, "Katarzyna Biała", "katarzyna@example.com", "admin", false);
            user.isActiveProperty().set(true);
            assertTrue(user.getIsActive());
        }
    @Test
    void trainingConstructor_ShouldInitializeFieldsCorrectly() {
        var training = new AdminDashboardController.Training(
                1, "2025-06-01", "Intensywny trening siłowy", "Jan Kowalski", "Anna Nowak"
        );

        assertEquals(1, training.getId());
        assertEquals("2025-06-01", training.getTrainingDate());
        assertEquals("Intensywny trening siłowy", training.getNotes());
        assertEquals("Jan Kowalski", training.getClientName());
        assertEquals("Anna Nowak", training.getTrainerName());
    }

    @Test
    void setTrainingDate_ShouldUpdateDateCorrectly() {
        var training = new AdminDashboardController.Training(
                2, "2025-05-15", "Trening ogólnorozwojowy", "Ewa Zielińska", "Tomasz Lis"
        );

        training.setTrainingDate("2025-06-10");
        assertEquals("2025-06-10", training.getTrainingDate());
    }

    @Test
    void setNotes_ShouldUpdateNotesCorrectly() {
        var training = new AdminDashboardController.Training(
                3, "2025-06-01", "Pierwsza sesja", "Marek Wójcik", "Karolina Nowak"
        );

        training.setNotes("Zmienione notatki treningowe");
        assertEquals("Zmienione notatki treningowe", training.getNotes());
    }

    @Test
    void setTrainerName_ShouldUpdateTrainerCorrectly() {
        var training = new AdminDashboardController.Training(
                4, "2025-06-01", "Sesja cardio", "Adam Krawczyk", "Stary Trener"
        );

        training.setTrainerName("Nowy Trener");
        assertEquals("Nowy Trener", training.getTrainerName());
    }
    @Test
    void membershipConstructor_ShouldInitializeFieldsCorrectly() {
        var membership = new AdminDashboardController.Membership(
                101, "Katarzyna Malinowska", 199.99, "2025-06-01"
        );

        assertEquals(101, membership.getId());
        assertEquals("Katarzyna Malinowska", membership.getClientName());
        assertEquals(199.99, membership.getAmount(), 0.001);
        assertEquals("2025-06-01", membership.getPaymentDate());
    }

    @Test
    void setAmount_ShouldUpdateAmountCorrectly() {
        var membership = new AdminDashboardController.Membership(
                102, "Piotr Zieliński", 150.00, "2025-05-20"
        );

        membership.setAmount(175.50);
        assertEquals(175.50, membership.getAmount(), 0.001);
    }

    @Test
    void setPaymentDate_ShouldUpdateDateCorrectly() {
        var membership = new AdminDashboardController.Membership(
                103, "Anna Kowalczyk", 180.00, "2025-05-01"
        );

        membership.setPaymentDate("2025-06-10");
        assertEquals("2025-06-10", membership.getPaymentDate());
    }
    @Test
    void constructor_ShouldInitializeFieldsCorrectly() {
        var transaction = new AdminDashboardController.Transaction(
                201, "Adam Nowak", "Baton Proteinowy", "2025-06-05", 15.99
        );

        assertEquals(201, transaction.getId());
        assertEquals("Adam Nowak", transaction.getClientName());
        assertEquals("Baton Proteinowy", transaction.getProductName());
        assertEquals("2025-06-05", transaction.getTransactionDate());
        assertEquals(15.99, transaction.getAmount(), 0.001);
    }

    @Test
    void setClientName_ShouldUpdateClientName() {
        var transaction = new AdminDashboardController.Transaction(
                202, "Jan Kowalski", "Woda", "2025-06-01", 5.00
        );

        transaction.setClientName("Maria Kwiatkowska");
        assertEquals("Maria Kwiatkowska", transaction.getClientName());
    }

    @Test
    void setAmount_ShouldUpdateAmount() {
        var transaction = new AdminDashboardController.Transaction(
                203, "Zofia Wiśniewska", "Szejk Białkowy", "2025-06-03", 12.00
        );

        transaction.setAmount(14.50);
        assertEquals(14.50, transaction.getAmount(), 0.001);
    }

    @Test
    void setTransactionDate_ShouldUpdateDate() {
        var transaction = new AdminDashboardController.Transaction(
                204, "Kamil Król", "Suplement Omega-3", "2025-06-02", 29.99
        );

        transaction.setTransactionDate("2025-06-08");
        assertEquals("2025-06-08", transaction.getTransactionDate());
    }

    @Test
    void getNameAndDisplayName_ShouldReturnSameValue() {
        AdminDashboardController.ProductDataImpl product = new AdminDashboardController.ProductDataImpl(2, "Energy Drink", 4.50, 100);

        assertEquals(product.getName(), product.getDisplayName());
    }

    @Test
    void getClientNameAndDisplayName_ShouldReturnSameValue() {
        AdminDashboardController.MembershipDataImpl membership = new AdminDashboardController.MembershipDataImpl(2, "Anna Nowak", 150.0, "2025-05-15");

        assertEquals(membership.getClientName(), membership.getDisplayName());
    }

    @Test
    void getDisplayName_ShouldReturnClientNameAndProductNameConcatenated() {
        AdminDashboardController.TransactionDataImpl transaction = new AdminDashboardController.TransactionDataImpl(20, "Anna Nowak", "Protein Powder", "2025-05-15", 49.95);

        assertEquals("Anna Nowak - Protein Powder", transaction.getDisplayName());
    }
    }
