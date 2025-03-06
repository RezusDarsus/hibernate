package com.test.rez;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TraineeDao {

    private static final Logger logger = LoggerFactory.getLogger(TraineeDao.class);

    public void createTraineeProfile(Trainee trainee) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction et = em.getTransaction();
        try {
            et.begin();
            em.persist(trainee.getUser());
            em.persist(trainee);
            et.commit();
            logger.info("Created trainee profile for username: {}", trainee.getUser().getUsername());
        } catch(Exception e) {
            if(et.isActive()) et.rollback();
            logger.error("Error creating trainee profile", e);
        } finally {
            em.close();
        }
    }

    public Trainee authenticateTrainee(String username, String password) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT t FROM Trainee t WHERE t.user.username = :username " +
                    "AND t.user.password = :password AND t.isActive = true";
            TypedQuery<Trainee> query = em.createQuery(jpql, Trainee.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            Trainee trainee = query.getSingleResult();
            logger.info("Trainee authenticated: {}", username);
            return trainee;
        } catch (Exception e) {
            logger.error("Trainee authentication failed for username: {}", username, e);
            return null;
        } finally {
            em.close();
        }
    }

    public Trainee selectTrainerbyUser(String username) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT t FROM Trainee t WHERE t.user.username = :username";
            TypedQuery<Trainee> query = em.createQuery(jpql, Trainee.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error selecting trainee by username: {}", username, e);
            return null;
        } finally {
            em.close();
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction et = em.getTransaction();
        try {
            Trainee trainee = authenticateTrainee(username, oldPassword);
            if (trainee == null) {
                logger.warn("Failed to change password: authentication failed for username: {}", username);
                return false;
            }
            et.begin();
            trainee.getUser().setPassword(newPassword);
            em.merge(trainee.getUser());
            et.commit();
            logger.info("Password changed for username: {}", username);
            return true;
        } catch (Exception e) {
            if (et.isActive()) et.rollback();
            logger.error("Error changing password for username: {}", username, e);
            return false;
        } finally {
            em.close();
        }
    }

    public void updateTrainee(Trainee trainee) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(trainee);
            tx.commit();
            logger.info("Trainee profile updated for username: {}", trainee.getUser().getUsername());
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Error updating trainee profile", e);
        } finally {
            em.close();
        }
    }

    public void setTraineeActive(String username, boolean active) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            String jpql = "SELECT t FROM Trainee t WHERE t.user.username = :username";
            TypedQuery<Trainee> query = em.createQuery(jpql, Trainee.class);
            query.setParameter("username", username);
            Trainee trainee = query.getSingleResult();
            if (trainee != null) {
                tx.begin();
                trainee.setActive(active);
                trainee.getUser().setActive(active);
                em.merge(trainee);
                tx.commit();
                logger.info("Trainee {} set to {}",
                        trainee.getUser().getUsername(),
                        active ? "active" : "inactive");
            }
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Error setting trainee active status for username: {}", username, e);
        } finally {
            em.close();
        }
    }

    public void deleteTraineeProfileByUsername(String username) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            String jpql = "SELECT t FROM Trainee t WHERE t.user.username = :username";
            TypedQuery<Trainee> query = em.createQuery(jpql, Trainee.class);
            query.setParameter("username", username);
            Trainee trainee = query.getSingleResult();
            if (trainee != null) {
                tx.begin();
                em.remove(trainee);
                tx.commit();
                logger.info("Trainee profile deleted for username: {}", username);
            }
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Error deleting trainee profile for username: {}", username, e);
        } finally {
            em.close();
        }
    }

    public List<Training> getTraineeTrainingsList(String username, Date fromDate, Date toDate, String trainerName, String trainingTypeName) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT tr FROM Training tr WHERE tr.trainee.user.username = :username " +
                    "AND tr.trainingDate BETWEEN :fromDate AND :toDate ";
            if (trainerName != null && !trainerName.isEmpty()) {
                jpql += "AND tr.trainer.user.firstName LIKE :trainerName ";
            }
            if (trainingTypeName != null && !trainingTypeName.isEmpty()) {
                jpql += "AND tr.trainingType.trainingTypeName = :trainingTypeName ";
            }
            TypedQuery<Training> query = em.createQuery(jpql, Training.class);
            query.setParameter("username", username);
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            if (trainerName != null && !trainerName.isEmpty()) {
                query.setParameter("trainerName", "%" + trainerName + "%");
            }
            if (trainingTypeName != null && !trainingTypeName.isEmpty()) {
                query.setParameter("trainingTypeName", trainingTypeName);
            }
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error retrieving trainee trainings list for username: {}", username, e);
            return null;
        } finally {
            em.close();
        }
    }

    public void updateTraineeTrainers(String username, List<Trainer> newTrainers) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            String jpql = "SELECT t FROM Trainee t WHERE t.user.username = :username";
            TypedQuery<Trainee> query = em.createQuery(jpql, Trainee.class);
            query.setParameter("username", username);
            Trainee trainee = query.getSingleResult();
            if (trainee != null) {
                tx.begin();
                trainee.setTrainers(newTrainers);
                em.merge(trainee);
                tx.commit();
                logger.info("Updated trainers list for trainee: {}", username);
            }
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.error("Error updating trainers list for username: {}", username, e);
        } finally {
            em.close();
        }
    }

    public List<Trainer> getTrainersNotAssignedToTrainee(String username) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpqlTrainee = "SELECT t FROM Trainee t WHERE t.user.username = :username";
            TypedQuery<Trainee> queryTrainee = em.createQuery(jpqlTrainee, Trainee.class);
            queryTrainee.setParameter("username", username);
            Trainee trainee = queryTrainee.getSingleResult();

            if (trainee == null) {
                logger.warn("Trainee not found for username: {}", username);
                return null;
            }

            List<Integer> assignedTrainerIds = trainee.getTrainers()
                    .stream()
                    .map(Trainer::getId)
                    .collect(Collectors.toList());

            String jpqlTrainer = "SELECT tr FROM Trainer tr";
            if (!assignedTrainerIds.isEmpty()) {
                jpqlTrainer += " WHERE Trainer.id NOT IN (:assignedIds)";
            }

            TypedQuery<Trainer> queryTrainer = em.createQuery(jpqlTrainer, Trainer.class);
            if (!assignedTrainerIds.isEmpty()) {
                queryTrainer.setParameter("assignedIds", assignedTrainerIds);
            }
            return queryTrainer.getResultList();
        } catch (Exception e) {
            logger.error("Error getting trainers not assigned to trainee: {}", username, e);
            return null;
        } finally {
            em.close();
        }
    }
}
