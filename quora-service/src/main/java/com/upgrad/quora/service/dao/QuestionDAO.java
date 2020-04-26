package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Returns Question details by questionId
     *
     * @param questionId
     * @return QuestionEntity
     */
    public QuestionEntity getQuestionByQuestionId(String questionId) {
        try {
            return entityManager.createNamedQuery("questionByQuestionId", QuestionEntity.class).setParameter("question_id", questionId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Get all questions
     *
     * @return List<QuestionEntity
     */
    public List<QuestionEntity> getAllQuestions() {
        try {
            return entityManager.createNamedQuery("allQuestions", QuestionEntity.class).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Submits user question
     *
     * @param questionEntity
     * @return QuestionEntity
     */
    public QuestionEntity submitQuestion(QuestionEntity questionEntity) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.merge(questionEntity);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
        return questionEntity;
    }

    /**
     * Delete question
     *
     * @param questionId
     */
    public void deleteQuestion(String questionId) {
        entityManager.remove(questionId);
    }
}
