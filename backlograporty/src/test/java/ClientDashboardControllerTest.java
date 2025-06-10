import static org.junit.jupiter.api.Assertions.*;

import com.example.silowniaprojekt.ClientDashboardController;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClientDashboardControllerTest {

    @Test
    void testClientActivityCreationAndProperties() {
        // given
        String expectedType = "Transakcja";
        String expectedDescription = "Zakup karnetu miesięcznego";
        BigDecimal expectedAmount = new BigDecimal("150.00");
        Timestamp expectedDate = Timestamp.valueOf("2025-06-08 10:30:00");
        String expectedStatus = "Zakończona";

        // when
        ClientDashboardController.ClientActivity activity =
                new ClientDashboardController.ClientActivity(expectedType, expectedDescription, expectedAmount, expectedDate, expectedStatus);

        // then - sprawdzenie getterów
        assertEquals(expectedType, activity.getType());
        assertEquals(expectedDescription, activity.getDescription());
        assertEquals(0, expectedAmount.compareTo(activity.getAmount()));
        assertEquals(expectedDate, activity.getDate());
        assertEquals(expectedStatus, activity.getStatus());

        // sprawdzenie właściwości JavaFX
        assertEquals(expectedType, activity.typeProperty().get());
        assertEquals(expectedDescription, activity.descriptionProperty().get());
        assertEquals(0, expectedAmount.compareTo(activity.amountProperty().get()));
        assertEquals(expectedDate, activity.dateProperty().get());
        assertEquals(expectedStatus, activity.statusProperty().get());
    }
    @Test
    void testClientScheduledTrainingCreationAndProperties() {
        // given
        String expectedTrainerName = "Jan Kowalski";
        LocalDateTime expectedTrainingDateTime = LocalDateTime.of(2025, 6, 10, 15, 30);
        String expectedNotes = "Trening siłowy";

        // when
        ClientDashboardController.ClientScheduledTraining training = new ClientDashboardController.ClientScheduledTraining(expectedTrainerName, expectedTrainingDateTime, expectedNotes);

        // then - sprawdzenie getterów
        assertEquals(expectedTrainerName, training.getTrainerName());
        assertEquals(expectedTrainingDateTime, training.getTrainingDateTime());
        assertEquals(expectedNotes, training.getNotes());

        // sprawdzenie właściwości JavaFX
        assertEquals(expectedTrainerName, training.trainerNameProperty().get());
        assertEquals(expectedTrainingDateTime, training.trainingDateTimeProperty().get());
        assertEquals(expectedNotes, training.notesProperty().get());
    }
    @Test
    void testClientTaskCreationAndGetters() {
        // given
        int expectedTaskId = 1;
        int expectedTrainingRequestId = 10;
        LocalDate expectedTrainingDate = LocalDate.of(2025, 6, 8);
        String expectedTaskDescription = "Zadanie do wykonania";
        int expectedPriorityId = 2;
        String expectedPriorityName = "Średni";
        int expectedStatusId = 3;
        String expectedStatusName = "W trakcie";
        int expectedTrainerId = 5;
        String expectedTrainerName = "Jan Kowalski";

        // when
        ClientDashboardController.ClientTask clientTask = new ClientDashboardController.ClientTask(
                expectedTaskId,
                expectedTrainingRequestId,
                expectedTrainingDate,
                expectedTaskDescription,
                expectedPriorityId,
                expectedPriorityName,
                expectedStatusId,
                expectedStatusName,
                expectedTrainerId,
                expectedTrainerName
        );

        // then - sprawdzenie getterów
        assertEquals(expectedTaskId, clientTask.getTaskId());
        assertEquals(expectedTrainingRequestId, clientTask.getTrainingRequestId());
        assertEquals(expectedTrainingDate, clientTask.getTrainingDate());
        assertEquals(expectedTaskDescription, clientTask.getTaskDescription());
        assertEquals(expectedPriorityId, clientTask.getPriorityId());
        assertEquals(expectedPriorityName, clientTask.getPriorityName());
        assertEquals(expectedStatusId, clientTask.getStatusId());
        assertEquals(expectedStatusName, clientTask.getStatusName());
        assertEquals(expectedTrainerId, clientTask.getTrainerId());
        assertEquals(expectedTrainerName, clientTask.getTrainerName());

        // sprawdzenie właściwości JavaFX
        assertEquals(expectedTaskId, clientTask.taskIdProperty().get());
        assertEquals(expectedTrainingRequestId, clientTask.trainingRequestIdProperty().get());
        assertEquals(expectedTrainingDate, clientTask.trainingDateProperty().get());
        assertEquals(expectedTaskDescription, clientTask.taskDescriptionProperty().get());
        assertEquals(expectedPriorityId, clientTask.priorityIdProperty().get());
        assertEquals(expectedPriorityName, clientTask.priorityNameProperty().get());
        assertEquals(expectedStatusId, clientTask.statusIdProperty().get());
        assertEquals(expectedStatusName, clientTask.statusNameProperty().get());
        assertEquals(expectedTrainerId, clientTask.trainerIdProperty().get());
        assertEquals(expectedTrainerName, clientTask.trainerNameProperty().get());
    }
    @Test
    void testClientTrainingRequestCreationAndGetters() {
        // given
        String expectedTrainerName = "Anna Nowak";
        String expectedRequestNotes = "Proszę o trening w godzinach wieczornych";
        Date expectedTrainingDate = Date.valueOf("2025-06-15");
        String expectedStatus = "Zaplanowany";

        // when
        ClientDashboardController.ClientTrainingRequest request = new ClientDashboardController.ClientTrainingRequest(
                expectedTrainerName,
                expectedRequestNotes,
                expectedTrainingDate,
                expectedStatus
        );

        // then - sprawdzenie getterów
        assertEquals(expectedTrainerName, request.getTrainerName());
        assertEquals(expectedRequestNotes, request.getRequestNotes());
        assertEquals(expectedTrainingDate, request.getTrainingDate());
        assertEquals(expectedStatus, request.getStatus());

        // sprawdzenie właściwości JavaFX
        assertEquals(expectedTrainerName, request.trainerNameProperty().get());
        assertEquals(expectedRequestNotes, request.requestNotesProperty().get());
        assertEquals(expectedTrainingDate, request.trainingDateProperty().get());
        assertEquals(expectedStatus, request.statusProperty().get());
    }

    @Test
    void testClientTrainingRequestWithNullDate() {
        // given
        String expectedTrainerName = "Piotr Zalewski";
        String expectedRequestNotes = "Czekam na potwierdzenie terminu";
        Date expectedTrainingDate = null;
        String expectedStatus = "Oczekujący";

        // when
        ClientDashboardController.ClientTrainingRequest request = new ClientDashboardController.ClientTrainingRequest(
                expectedTrainerName,
                expectedRequestNotes,
                expectedTrainingDate,
                expectedStatus
        );

        // then
        assertEquals(expectedTrainerName, request.getTrainerName());
        assertEquals(expectedRequestNotes, request.getRequestNotes());
        assertNull(request.getTrainingDate());
        assertEquals(expectedStatus, request.getStatus());
    }
    @Test
    void testPriority() {
        int expectedId = 1;
        String expectedName = "Wysoki";

        ClientDashboardController.Priority priority = new ClientDashboardController.Priority(expectedId, expectedName);

        assertEquals(expectedId, priority.getId());
        assertEquals(expectedName, priority.getName());

        // Test właściwości
        assertEquals(expectedId, priority.idProperty().get());
        assertEquals(expectedName, priority.nameProperty().get());

        // Test toString zwraca nazwę
        assertEquals(expectedName, priority.toString());
    }

    @Test
    void testStatus() {
        int expectedId = 2;
        String expectedName = "W trakcie";

        ClientDashboardController.Status status = new ClientDashboardController.Status(expectedId, expectedName);

        assertEquals(expectedId, status.getId());
        assertEquals(expectedName, status.getName());

        // Test właściwości
        assertEquals(expectedId, status.idProperty().get());
        assertEquals(expectedName, status.nameProperty().get());

        // Test toString zwraca nazwę
        assertEquals(expectedName, status.toString());
    }
    @Test
    void testTrainer() {
        int expectedId = 10;
        String expectedName = "Jan Kowalski";

        ClientDashboardController.Trainer trainer = new ClientDashboardController.Trainer(expectedId, expectedName);

        assertEquals(expectedId, trainer.getId());
        assertEquals(expectedName, trainer.getName());

        // Sprawdzenie toString() - powinno zwrócić imię i nazwisko
        assertEquals(expectedName, trainer.toString());
    }
}
