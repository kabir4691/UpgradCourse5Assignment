package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

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
    public QuestionEntity getQuestionByQuestionId(Integer questionId) {
        try {
            return entityManager.createNamedQuery("questionByQuestionId", QuestionEntity.class).setParameter("question_id", questionId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
