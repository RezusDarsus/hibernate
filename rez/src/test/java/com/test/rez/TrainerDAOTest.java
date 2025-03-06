package com.test.rez;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class TrainerDAOTest {

    private TrainerDAO trainerDAO;

    @BeforeEach
    public void setup() {
        trainerDAO = new TrainerDAO();
    }

    @Test
    public void testCreateTrainerProfile() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("secret");
        user.setActive(true);

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Pilates");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);
        trainer.setActive(true);

        trainerDAO.createTrainerProfile(trainer);

        Trainer retrieved = trainerDAO.getTrainerByUsername("john.doe");
        assertNotNull(retrieved, "Trainer should be retrieved successfully");
        assertEquals("john.doe", retrieved.getUser().getUsername());
    }

    @Test
    public void testAuthenticateTrainer_Success() {
        User user = new User();
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setUsername("alice.smith");
        user.setPassword("password123");
        user.setActive(true);

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);
        trainer.setActive(true);

        trainerDAO.createTrainerProfile(trainer);

        Trainer authTrainer = trainerDAO.authenticateTrainer("alice.smith", "password123");
        assertNotNull(authTrainer, "Authentication should succeed for valid credentials");
        assertEquals("Alice", authTrainer.getUser().getFirstName());
    }

    @Test
    public void testChangeTrainerPassword() {
        User user = new User();
        user.setFirstName("Bob");
        user.setLastName("Brown");
        user.setUsername("bob.brown");
        user.setPassword("oldPass");
        user.setActive(true);

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("CrossFit");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);
        trainer.setActive(true);

        trainerDAO.createTrainerProfile(trainer);

        boolean changed = trainerDAO.changeTrainerPassword("bob.brown", "oldPass", "newPass");
        assertTrue(changed, "Password should be changed successfully");

        Trainer authenticated = trainerDAO.authenticateTrainer("bob.brown", "newPass");
        assertNotNull(authenticated, "Authentication should succeed with new password");
    }
}
