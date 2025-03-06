package com.test.rez;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class TraineeDaoTest {

    private TraineeDao traineeDao;

    @BeforeEach
    public void setup() {
        traineeDao = new TraineeDao();
    }

    @Test
    public void testCreateTraineeProfile() {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setUsername("jane.doe");
        user.setPassword("secret");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setActive(true);
        trainee.setDateOfBirth(new Date());

        traineeDao.createTraineeProfile(trainee);

        Trainee retrieved = traineeDao.selectTrainerbyUser("jane.doe");
        assertNotNull(retrieved, "Trainee should be retrieved successfully");
        assertEquals("jane.doe", retrieved.getUser().getUsername());
    }

    @Test
    public void testChangePassword() {
        User user = new User();
        user.setFirstName("Mark");
        user.setLastName("Smith");
        user.setUsername("mark.smith");
        user.setPassword("oldPassword");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setActive(true);
        trainee.setDateOfBirth(new Date());

        traineeDao.createTraineeProfile(trainee);

        boolean changed = traineeDao.changePassword("mark.smith", "oldPassword", "newPassword");
        assertTrue(changed, "Password should be changed successfully");

        Trainee authenticated = traineeDao.authenticateTrainee("mark.smith", "newPassword");
        assertNotNull(authenticated, "Trainee should authenticate with new password");
    }
}
