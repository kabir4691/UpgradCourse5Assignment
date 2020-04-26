package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;

@Repository
public class AnswerDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates answer for the specific question
     *
     * @param answerEntity
     * @return AnswerEntity
     */
    public AnswerEntity submitAnswer(AnswerEntity answerEntity) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.merge(answerEntity);
            transaction.commit();
        } catch (Exception e) {
            // Rollback the transaction if there are any failures
            transaction.rollback();
        }
        return answerEntity;
    }

    /**
     * Returns answer based on answerId
     *
     * @param answerId
     * @return AnswerEntity
     */
    public AnswerEntity getAnswer(String answerId) {
        return entityManager.find(AnswerEntity.class, answerId);
    }

    /**
     * Delete answer by answer Id
     *
     * @param answerId
     */
    public void deleteAnswer(String answerId) {
        entityManager.remove(answerId);
    }

}
