package com.test.rez;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class TrainerDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainerDAO.class);

    public void createTrainerProfile(Trainer trainer) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (trainer.getSpecialization() != null && trainer.getSpecialization().getId() == 0) {
                em.persist(trainer.getSpecialization());
            }
            em.persist(trainer.getUser());
            em.persist(trainer);
            tx.commit();
            logger.info("Trainer profile created for username: {}", trainer.getUser().getUsername());
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Error creating trainer profile for username: {}", trainer.getUser().getUsername(), e);
        } finally {
            em.close();
        }
    }

    public Trainer authenticateTrainer(String username, String password) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT t FROM Trainer t WHERE t.user.username = :username " +
                    "AND t.user.password = :password AND t.isActive = true";
            TypedQuery<Trainer> query = em.createQuery(jpql, Trainer.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            Trainer trainer = query.getSingleResult();
            logger.info("Trainer authenticated for username: {}", username);
            return trainer;
        } catch (Exception e) {
            logger.error("Error authenticating trainer for username: {}", username, e);
            return null;
        } finally {
            em.close();
        }
    }

    public Trainer getTrainerByUsername(String username) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT t FROM Trainer t WHERE t.user.username = :username";
            TypedQuery<Trainer> query = em.createQuery(jpql, Trainer.class);
            query.setParameter("username", username);
            Trainer trainer = query.getSingleResult();
            logger.info("Retrieved trainer for username: {}", username);
            return trainer;
        } catch (Exception e) {
            logger.error("Error retrieving trainer for username: {}", username, e);
            return null;
        } finally {
            em.close();
        }
    }

    public boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Trainer trainer = authenticateTrainer(username, oldPassword);
            if (trainer == null) {
                logger.warn("Authentication failed for trainer: {}. Password change aborted.", username);
                return false;
            }
            tx.begin();
            trainer.getUser().setPassword(newPassword);
            em.merge(trainer.getUser());
            tx.commit();
            logger.info("Password changed for trainer: {}", username);
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Error changing password for trainer: {}", username, e);
            return false;
        } finally {
            em.close();
        }
    }

    public void updateTrainerProfile(Trainer trainer) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(trainer);
            tx.commit();
            logger.info("Trainer profile updated for username: {}", trainer.getUser().getUsername());
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Error updating trainer profile for username: {}", trainer.getUser().getUsername(), e);
        } finally {
            em.close();
        }
    }

    public void setTrainerActive(String username, boolean active) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            String jpql = "SELECT t FROM Trainer t WHERE t.user.username = :username";
            TypedQuery<Trainer> query = em.createQuery(jpql, Trainer.class);
            query.setParameter("username", username);
            Trainer trainer = query.getSingleResult();
            if (trainer != null) {
                tx.begin();
                trainer.setActive(active);
                trainer.getUser().setActive(active);
                em.merge(trainer);
                tx.commit();
                logger.info("Trainer {} for username: {}", active ? "activated" : "deactivated", username);
            }
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Error setting active status for trainer: {}", username, e);
        } finally {
            em.close();
        }
    }

    public List<Training> getTrainerTrainingsList(String username, Date fromDate, Date toDate, String traineeName) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT tr FROM Training tr WHERE tr.trainer.user.username = :username " +
                    "AND tr.trainingDate BETWEEN :fromDate AND :toDate ";
            if (traineeName != null && !traineeName.isEmpty()) {
                jpql += "AND tr.trainee.user.firstName LIKE :traineeName ";
            }
            TypedQuery<Training> query = em.createQuery(jpql, Training.class);
            query.setParameter("username", username);
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            if (traineeName != null && !traineeName.isEmpty()) {
                query.setParameter("traineeName", "%" + traineeName + "%");
            }
            List<Training> trainings = query.getResultList();
            logger.info("Retrieved {} trainings for trainer: {}", trainings.size(), username);
            return trainings;
        } catch (Exception e) {
            logger.error("Error retrieving trainings for trainer: {}", username, e);
            return null;
        } finally {
            em.close();
        }
    }
}
