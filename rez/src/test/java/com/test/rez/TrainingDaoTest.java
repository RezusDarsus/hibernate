package com.test.rez;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class TrainingDaoTest {

    private TrainingDao trainingDao;

    @BeforeEach
    public void setup() {
        trainingDao = new TrainingDao();
    }

    @Test
    public void testAddTraining() {
        // --- Prepare related entities needed for Training ---

        // 1. Create and persist a TrainingType.
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("TestType");
        persistEntity(trainingType);

        // 2. Create and persist a Trainer.
        User trainerUser = new User();
        trainerUser.setFirstName("Trainer");
        trainerUser.setLastName("One");
        trainerUser.setUsername("trainer.one");
        trainerUser.setPassword("pass");
        trainerUser.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(trainingType);
        trainer.setActive(true);
        persistEntity(trainerUser); // persist user first
        persistEntity(trainer);

        // 3. Create and persist a Trainee.
        User traineeUser = new User();
        traineeUser.setFirstName("Trainee");
        traineeUser.setLastName("One");
        traineeUser.setUsername("trainee.one");
        traineeUser.setPassword("pass");
        traineeUser.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        trainee.setActive(true);
        trainee.setDateOfBirth(new Date());
        persistEntity(traineeUser);
        persistEntity(trainee);

        // --- Create the Training entity ---
        Training training = new Training();
        training.setTrainingDuration(60); // Duration in minutes
        training.setTrainingDate(new Date());
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setTrainingType(trainingType);

        // --- Use the TrainingDao to add the training ---
        trainingDao.addTraining(training);

        // --- Verify the training was persisted ---
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        Training persistedTraining = em.find(Training.class, training.getId());
        em.close();
        assertNotNull(persistedTraining, "Training should be persisted and retrievable");
    }

    /**
     * Helper method to persist an entity using a new EntityManager.
     * This method starts and commits a transaction.
     */
    private void persistEntity(Object entity) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
