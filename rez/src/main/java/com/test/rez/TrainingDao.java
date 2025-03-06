package com.test.rez;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainingDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainingDao.class);

    public void addTraining(Training training) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(training);
            tx.commit();
            logger.info("Training added successfully with ID: {}", training.getId());
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error adding training", e);
        } finally {
            em.close();
        }
    }
}
