import com.example.silowniaprojekt.TrainerDashboardController;
import org.junit.jupiter.api.Test;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TrainerDashboardControllerTest {

    @Test
    public void testClientConstructorAndGetters() {
        // Arrange
        int expectedId = 1;
        String expectedName = "Anna Kowalska";

        // Act
        TrainerDashboardController.Client client = new TrainerDashboardController.Client(expectedId, expectedName);

        // Assert
        assertEquals(expectedId, client.getId());
        assertEquals(expectedName, client.getName());

        assertEquals(expectedId, client.idProperty().get());
        assertEquals(expectedName, client.nameProperty().get());

        assertEquals(expectedName, client.toString());
    }
    @Test
    public void testPendingTrainingRequestConstructorAndGetters() {
        // Arrange
        int expectedId = 101;
        int expectedClientId = 55;
        String expectedClientName = "Jan Nowak";
        String expectedNotes = "Preferowany trening wieczorem";

        // Act
        TrainerDashboardController.PendingTrainingRequest request =
                new TrainerDashboardController.PendingTrainingRequest(expectedId, expectedClientId, expectedClientName, expectedNotes);

        // Assert
        assertEquals(expectedId, request.getId());
        assertEquals(expectedClientId, request.getClientId());
        assertEquals(expectedClientName, request.getClientName());
        assertEquals(expectedNotes, request.getNotes());

        assertEquals(expectedId, request.idProperty().get());
        assertEquals(expectedClientId, request.clientIdProperty().get());
        assertEquals(expectedClientName, request.clientNameProperty().get());
        assertEquals(expectedNotes, request.notesProperty().get());
    }
    @Test
    public void testPriorityConstructorAndGetters() {
        // Arrange
        int expectedId = 1;
        String expectedName = "Wysoki";

        // Act
        TrainerDashboardController.Priority priority =
                new TrainerDashboardController.Priority(expectedId, expectedName);

        // Assert
        assertEquals(expectedId, priority.getId());
        assertEquals(expectedName, priority.getName());

        assertEquals(expectedId, priority.idProperty().get());
        assertEquals(expectedName, priority.nameProperty().get());

        assertEquals(expectedName, priority.toString());
    }
    @Test
    public void testProductConstructorAndGetters() {
        // Arrange
        int expectedId = 101;
        String expectedName = "Baton Proteinowy";
        BigDecimal expectedPrice = new BigDecimal("12.50");
        int expectedStock = 30;

        // Act
        TrainerDashboardController.Product product =
                new TrainerDashboardController.Product(expectedId, expectedName, expectedPrice, expectedStock);

        // Assert
        assertEquals(expectedId, product.getId());
        assertEquals(expectedName, product.getName());
        assertEquals(expectedPrice, product.getPrice());
        assertEquals(expectedStock, product.getStock());

        assertEquals(expectedId, product.idProperty().get());
        assertEquals(expectedName, product.nameProperty().get());
        assertEquals(expectedPrice, product.priceProperty().get());
        assertEquals(expectedStock, product.stockProperty().get());
    }
    @Test
    public void testReportEntryConstructorAndGetters() {
        // Arrange
        int expectedId = 10;
        int expectedClientId = 5;
        String expectedClientName = "Jan Kowalski";
        String expectedNotes = "Dobre postępy w treningu siłowym.";
        String expectedTrainerName = "Anna Nowak";

        // Act
        TrainerDashboardController.ReportEntry reportEntry = new TrainerDashboardController.ReportEntry(
                expectedId, expectedClientId, expectedClientName, expectedNotes, expectedTrainerName
        );

        // Assert - gettery
        assertEquals(expectedId, reportEntry.getId());
        assertEquals(expectedClientId, reportEntry.getClientId());
        assertEquals(expectedClientName, reportEntry.getClientName());
        assertEquals(expectedNotes, reportEntry.getNotes());
        assertEquals(expectedTrainerName, reportEntry.getTrainerName());

        // Assert - właściwości
        assertEquals(expectedId, reportEntry.idProperty().get());
        assertEquals(expectedClientId, reportEntry.clientIdProperty().get());
        assertEquals(expectedClientName, reportEntry.clientNameProperty().get());
        assertEquals(expectedNotes, reportEntry.notesProperty().get());
        assertEquals(expectedTrainerName, reportEntry.trainerNameProperty().get());
    }
    @Test
    public void testStatusConstructorAndGetters() {
        // Arrange
        int expectedId = 3;
        String expectedName = "w trakcie";

        // Act
        TrainerDashboardController.Status status = new TrainerDashboardController.Status(expectedId, expectedName);

        // Assert - gettery
        assertEquals(expectedId, status.getId());
        assertEquals(expectedName, status.getName());

        // Assert - właściwości
        assertEquals(expectedId, status.idProperty().get());
        assertEquals(expectedName, status.nameProperty().get());
    }

    @Test
    public void testToStringReturnsName() {
        // Arrange
        String name = "zakończone";
        TrainerDashboardController.Status status = new TrainerDashboardController.Status(4, name);

        // Act & Assert
        assertEquals(name, status.toString());
    }
    @Test
    public void testTrainerIncomeEntryConstructorAndGetters() {
        // Arrange
        int paymentId = 42;
        String clientName = "Anna Kowalska";
        LocalDateTime trainingDateTime = LocalDateTime.of(2024, 5, 20, 10, 30);
        Timestamp paymentDate = Timestamp.valueOf("2024-05-21 15:00:00");
        BigDecimal amount = new BigDecimal("150.00");
        String trainingNotes = "Trening siłowy i rozciąganie";

        // Act
        TrainerDashboardController.TrainerIncomeEntry entry =
                new TrainerDashboardController.TrainerIncomeEntry(
                        paymentId, clientName, trainingDateTime, paymentDate, amount, trainingNotes
                );

        // Assert - Gettery
        assertEquals(paymentId, entry.getPaymentId());
        assertEquals(clientName, entry.getClientName());
        assertEquals(trainingDateTime, entry.getTrainingDateTime());
        assertEquals(paymentDate, entry.getPaymentDate());
        assertEquals(amount, entry.getAmount());
        assertEquals(trainingNotes, entry.getTrainingNotes());

        // Assert - Properties
        assertEquals(paymentId, entry.paymentIdProperty().get());
        assertEquals(clientName, entry.clientNameProperty().get());
        assertEquals(trainingDateTime, entry.trainingDateTimeProperty().get());
        assertEquals(paymentDate, entry.paymentDateProperty().get());
        assertEquals(amount, entry.amountProperty().get());
        assertEquals(trainingNotes, entry.trainingNotesProperty().get());
    }
    @Test
    public void testTrainerTaskConstructorAndGetters() {
        // Arrange
        int id = 1;
        int trainingRequestId = 10;
        int clientId = 5;
        String clientName = "Jan Kowalski";
        LocalDate trainingDate = LocalDate.of(2025, 6, 10);
        String taskDescription = "Ułożyć plan treningowy na siłę";
        int priorityId = 2;
        int statusId = 3;

        // Act
        TrainerDashboardController.TrainerTask task = new TrainerDashboardController.TrainerTask(
                id, trainingRequestId, clientId, clientName, trainingDate, taskDescription, priorityId, statusId
        );

        // Assert
        assertEquals(id, task.getId());
        assertEquals(trainingRequestId, task.getTrainingRequestId());
        assertEquals(clientId, task.getClientId());
        assertEquals(clientName, task.getClientName());
        assertEquals(trainingDate, task.getTrainingDate());
        assertEquals(taskDescription, task.getTaskDescription());
        assertEquals(priorityId, task.getPriorityId());
        assertEquals(statusId, task.getStatusId());
    }
    @Test
    public void testTrainingScheduleEntryConstructorAndGetters() {
        int id = 42;
        String clientName = "Anna Nowak";
        LocalDate trainingDate = LocalDate.of(2025, 6, 8);
        String notes = "Skupić się na rozciąganiu";

        TrainerDashboardController.TrainingScheduleEntry entry =
                new TrainerDashboardController.TrainingScheduleEntry(id, clientName, trainingDate, notes);

        // Test getterów
        assertEquals(id, entry.getId());
        assertEquals(clientName, entry.getClientName());
        assertEquals(trainingDate, entry.getTrainingDate());
        assertEquals(notes, entry.getNotes());

        // Test właściwości JavaFX
        assertEquals(id, entry.idProperty().get());
        assertEquals(clientName, entry.clientNameProperty().get());
        assertEquals(trainingDate, entry.trainingDateProperty().get());
        assertEquals(notes, entry.notesProperty().get());
    }
}
