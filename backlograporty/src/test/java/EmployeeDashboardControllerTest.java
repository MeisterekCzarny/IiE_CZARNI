import static org.junit.jupiter.api.Assertions.*;

import com.example.silowniaprojekt.EmployeeDashboardController;
import org.junit.jupiter.api.Test;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class EmployeeDashboardControllerTest {

    @Test
    void testEquipmentProperties() {
        // given
        String expectedName = "Laptop";
        int expectedQuantity = 10;
        String expectedStatus = "Dostępne";
        String expectedLastCheck = "2025-06-01";

        // when
        EmployeeDashboardController.Equipment equipment =
                new EmployeeDashboardController.Equipment(expectedName, expectedQuantity, expectedStatus, expectedLastCheck);

        // then
        assertEquals(expectedName, equipment.getName());
        assertEquals(expectedQuantity, equipment.getQuantity());
        assertEquals(expectedStatus, equipment.getStatus());
        assertEquals(expectedLastCheck, equipment.getLastCheck());

        // sprawdzanie właściwości (property)
        assertEquals(expectedName, equipment.nameProperty().get());
        assertEquals(expectedQuantity, equipment.quantityProperty().get());
        assertEquals(expectedStatus, equipment.statusProperty().get());
        assertEquals(expectedLastCheck, equipment.lastCheckProperty().get());
    }
    @Test
    void testMembershipPaymentProperties() {
        // given
        int expectedId = 101;
        int expectedClientId = 555;
        String expectedClientName = "Jan Kowalski";
        BigDecimal expectedAmount = new BigDecimal("199.99");
        Timestamp expectedPaymentDate = Timestamp.valueOf("2025-06-01 12:30:00");

        // when
        EmployeeDashboardController.MembershipPayment payment =
                new EmployeeDashboardController.MembershipPayment(
                        expectedId,
                        expectedClientId,
                        expectedClientName,
                        expectedAmount,
                        expectedPaymentDate
                );

        // then
        assertEquals(expectedId, payment.getId());
        assertEquals(expectedClientId, payment.getClientId());
        assertEquals(expectedClientName, payment.getClientName());
        assertEquals(expectedAmount, payment.getAmount());
        assertEquals(expectedPaymentDate, payment.getPaymentDate());

        // sprawdzenie właściwości (property)
        assertEquals(expectedId, payment.idProperty().get());
        assertEquals(expectedClientId, payment.clientIdProperty().get());
        assertEquals(expectedClientName, payment.clientNameProperty().get());
        assertEquals(expectedAmount, payment.amountProperty().get());
        assertEquals(expectedPaymentDate, payment.paymentDateProperty().get());
    }
    @Test
    void testPricingPackageProperties() {
        // given
        String expectedName = "Premium";
        String expectedDescription = "Dostęp do wszystkich zajęć i sauny";
        double expectedPrice = 299.99;
        String expectedDuration = "3 miesiące";
        String expectedStatus = "Aktywny";

        // when
        EmployeeDashboardController.PricingPackage pricingPackage =
                new EmployeeDashboardController.PricingPackage(
                        expectedName,
                        expectedDescription,
                        expectedPrice,
                        expectedDuration,
                        expectedStatus
                );

        // then
        assertEquals(expectedName, pricingPackage.getName());
        assertEquals(expectedDescription, pricingPackage.getDescription());
        assertEquals(expectedPrice, pricingPackage.getPrice(), 0.0001);
        assertEquals(expectedDuration, pricingPackage.getDuration());
        assertEquals(expectedStatus, pricingPackage.getStatus());

        // test właściwości (property)
        assertEquals(expectedName, pricingPackage.nameProperty().get());
        assertEquals(expectedDescription, pricingPackage.descriptionProperty().get());
        assertEquals(expectedPrice, pricingPackage.priceProperty().get(), 0.0001);
        assertEquals(expectedDuration, pricingPackage.durationProperty().get());
        assertEquals(expectedStatus, pricingPackage.statusProperty().get());
    }
    @Test
    void testProductProperties() {
        // given
        int expectedId = 101;
        String expectedName = "Białko serwatkowe";
        double expectedPrice = 129.99;
        int expectedStock = 50;

        // when
        EmployeeDashboardController.Product product =
                new EmployeeDashboardController.Product(expectedId, expectedName, expectedPrice, expectedStock);

        // then
        assertEquals(expectedId, product.getId());
        assertEquals(expectedName, product.getName());
        assertEquals(expectedPrice, product.getPrice(), 0.0001);
        assertEquals(expectedStock, product.getStock());

        // test właściwości (property)
        assertEquals(expectedId, product.idProperty().get());
        assertEquals(expectedName, product.nameProperty().get());
        assertEquals(expectedPrice, product.priceProperty().get(), 0.0001);
        assertEquals(expectedStock, product.stockProperty().get());

        // test metody toString()
        String expectedString = expectedName + " (sztuk: " + expectedStock + ")";
        assertEquals(expectedString, product.toString());
    }
    @Test
    void testScheduleProperties() {
        // given
        String expectedDate = "2025-06-08";
        String expectedTime = "18:00";
        String expectedActivity = "Joga";
        String expectedTrainer = "Anna Kowalska";
        int expectedParticipants = 12;
        String expectedStatus = "Aktywne";

        // when
        EmployeeDashboardController.Schedule schedule =
                new EmployeeDashboardController.Schedule(
                        expectedDate, expectedTime, expectedActivity, expectedTrainer, expectedParticipants, expectedStatus);

        // then
        assertEquals(expectedDate, schedule.getDate());
        assertEquals(expectedTime, schedule.getTime());
        assertEquals(expectedActivity, schedule.getActivity());
        assertEquals(expectedTrainer, schedule.getTrainer());
        assertEquals(expectedParticipants, schedule.getParticipants());
        assertEquals(expectedStatus, schedule.getStatus());

        // test właściwości (property)
        assertEquals(expectedDate, schedule.dateProperty().get());
        assertEquals(expectedTime, schedule.timeProperty().get());
        assertEquals(expectedActivity, schedule.activityProperty().get());
        assertEquals(expectedTrainer, schedule.trainerProperty().get());
        assertEquals(expectedParticipants, schedule.participantsProperty().get());
        assertEquals(expectedStatus, schedule.statusProperty().get());
    }
    @Test
    void testTrainingRequestEntry() {
        // given
        int expectedRequestId = 101;
        String expectedClientName = "Jan Nowak";
        String expectedTrainerName = "Ewa Zielińska";
        LocalDateTime expectedTrainingDateTime = LocalDateTime.of(2025, 6, 10, 15, 30);
        String expectedRequestNotes = "Proszę o trening siłowy.";
        String expectedStatus = "Zaplanowany";

        // when
        EmployeeDashboardController.TrainingRequestEntry requestEntry =
                new EmployeeDashboardController.TrainingRequestEntry(
                        expectedRequestId, expectedClientName, expectedTrainerName,
                        expectedTrainingDateTime, expectedRequestNotes, expectedStatus);

        // then
        assertEquals(expectedRequestId, requestEntry.getRequestId());
        assertEquals(expectedClientName, requestEntry.getClientName());
        assertEquals(expectedTrainerName, requestEntry.getTrainerName());
        assertEquals(expectedTrainingDateTime, requestEntry.getTrainingDateTime());
        assertEquals(expectedRequestNotes, requestEntry.getRequestNotes());
        assertEquals(expectedStatus, requestEntry.getStatus());

        // test properties
        assertEquals(expectedRequestId, requestEntry.requestIdProperty().get());
        assertEquals(expectedClientName, requestEntry.clientNameProperty().get());
        assertEquals(expectedTrainerName, requestEntry.trainerNameProperty().get());
        assertEquals(expectedTrainingDateTime, requestEntry.trainingDateTimeProperty().get());
        assertEquals(expectedRequestNotes, requestEntry.requestNotesProperty().get());
        assertEquals(expectedStatus, requestEntry.statusProperty().get());
    }

    @Test
    void testTrainingRequestEntryWithNullDateTime() {
        // given
        int expectedRequestId = 102;
        String expectedClientName = "Katarzyna Wiśniewska";
        String expectedTrainerName = "Marek Kowalczyk";
        LocalDateTime expectedTrainingDateTime = null;
        String expectedRequestNotes = "Oczekuje na przydział trenera.";
        String expectedStatus = "Oczekujący";

        // when
        EmployeeDashboardController.TrainingRequestEntry requestEntry =
                new EmployeeDashboardController.TrainingRequestEntry(
                        expectedRequestId, expectedClientName, expectedTrainerName,
                        expectedTrainingDateTime, expectedRequestNotes, expectedStatus);

        // then
        assertEquals(expectedRequestId, requestEntry.getRequestId());
        assertEquals(expectedClientName, requestEntry.getClientName());
        assertEquals(expectedTrainerName, requestEntry.getTrainerName());
        assertNull(requestEntry.getTrainingDateTime());
        assertEquals(expectedRequestNotes, requestEntry.getRequestNotes());
        assertEquals(expectedStatus, requestEntry.getStatus());

        // test properties
        assertEquals(expectedRequestId, requestEntry.requestIdProperty().get());
        assertEquals(expectedClientName, requestEntry.clientNameProperty().get());
        assertEquals(expectedTrainerName, requestEntry.trainerNameProperty().get());
        assertNull(requestEntry.trainingDateTimeProperty().get());
        assertEquals(expectedRequestNotes, requestEntry.requestNotesProperty().get());
        assertEquals(expectedStatus, requestEntry.statusProperty().get());
    }
    @Test
    void testTrainingRequestPaymentCreationAndGetters() {
        // given
        int expectedId = 1;
        Timestamp expectedPaymentDate = Timestamp.valueOf("2025-06-08 14:30:00");
        BigDecimal expectedAmount = new BigDecimal("150.50");

        // when
        EmployeeDashboardController.TrainingRequestPayment payment = new EmployeeDashboardController.TrainingRequestPayment(expectedId, expectedPaymentDate, expectedAmount);

        // then
        assertEquals(expectedId, payment.getId());
        assertEquals(expectedPaymentDate, payment.getPaymentDate());
        assertEquals(expectedAmount, payment.getAmount());

        // test properties
        assertEquals(expectedId, payment.idProperty().get());
        assertEquals(expectedPaymentDate, payment.paymentDateProperty().get());
        assertEquals(expectedAmount, payment.amountProperty().get());
    }
    @Test
    void testTransactionCreationAndGetters() {
        // given
        int expectedId = 101;
        int expectedClientId = 202;
        String expectedClientName = "Jan Kowalski";
        int expectedProductId = 303;
        String expectedProductName = "Mata do jogi";
        BigDecimal expectedAmount = new BigDecimal("99.99");
        Timestamp expectedTransactionDate = Timestamp.valueOf("2025-06-08 12:00:00");

        // when
        EmployeeDashboardController.Transaction transaction = new EmployeeDashboardController.Transaction(
                expectedId,
                expectedClientId,
                expectedClientName,
                expectedProductId,
                expectedProductName,
                expectedAmount,
                expectedTransactionDate
        );

        // then
        assertEquals(expectedId, transaction.getId());
        assertEquals(expectedClientId, transaction.getClientId());
        assertEquals(expectedClientName, transaction.getClientName());
        assertEquals(expectedProductId, transaction.getProductId());
        assertEquals(expectedProductName, transaction.getProductName());
        assertEquals(expectedAmount, transaction.getAmount());
        assertEquals(expectedTransactionDate, transaction.getTransactionDate());

        // test JavaFX properties
        assertEquals(expectedId, transaction.idProperty().get());
        assertEquals(expectedClientId, transaction.clientIdProperty().get());
        assertEquals(expectedClientName, transaction.clientNameProperty().get());
        assertEquals(expectedProductId, transaction.productIdProperty().get());
        assertEquals(expectedProductName, transaction.productNameProperty().get());
        assertEquals(expectedAmount, transaction.amountProperty().get());
        assertEquals(expectedTransactionDate, transaction.transactionDateProperty().get());
    }
    @Test
    void testUserCreationAndProperties() {
        // given
        int expectedId = 1;
        String expectedName = "Anna Nowak";
        String expectedEmail = "anna.nowak@example.com";
        String expectedRole = "trainer";
        boolean expectedIsActive = false;

        // when - używamy konstruktora z isActive
        EmployeeDashboardController.User user = new EmployeeDashboardController.User(expectedId, expectedName, expectedEmail, expectedRole, expectedIsActive);

        // then - sprawdzenie getterów
        assertEquals(expectedId, user.getId());
        assertEquals(expectedName, user.getName());
        assertEquals(expectedEmail, user.getEmail());
        assertEquals(expectedRole, user.getRole());
        assertEquals(expectedIsActive, user.getIsActive());

        // sprawdzenie właściwości JavaFX
        assertEquals(expectedId, user.idProperty().get());
        assertEquals(expectedName, user.nameProperty().get());
        assertEquals(expectedEmail, user.emailProperty().get());
        assertEquals(expectedRole, user.roleProperty().get());
        assertEquals(expectedIsActive, user.isActiveProperty().get());

        // when - zmiana statusu aktywności
        user.setIsActive(true);

        // then - sprawdzenie zmiany
        assertTrue(user.getIsActive());
        assertTrue(user.isActiveProperty().get());
    }

    @Test
    void testUserDefaultIsActive() {
        // given
        EmployeeDashboardController.User user = new EmployeeDashboardController.User(2, "Jan Kowalski", "jan.kowalski@example.com", "client");

        // then - domyślnie isActive powinno być true
        assertTrue(user.getIsActive());
        assertTrue(user.isActiveProperty().get());
    }

    @Test
    void testToString() {
        EmployeeDashboardController.User user = new EmployeeDashboardController.User(3, "Ewa Malinowska", "ewa.malinowska@example.com", "employee");

        assertEquals("Ewa Malinowska", user.toString());
    }
}
