package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AnswerDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private QuestionDAO questionDAO;

    /**
     * Creates answer for the specific question
     *
     * @param answerEntity
     * @return AnswerEntity
     */
    public AnswerEntity submitAnswer(AnswerEntity answerEntity) {
        entityManager.merge(answerEntity);
        return answerEntity;
    }

    /**
     * Returns answer based on answerId
     *
     * @param answerId
     * @return AnswerEntity
     */
    public AnswerEntity getAnswer(String answerId) {
        try {
            return entityManager.createNamedQuery("answerByAnswerId", AnswerEntity.class).setParameter("answerId", answerId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<AnswerEntity> getAllAnswers(Integer questionId) {
        try {
            return entityManager.createNamedQuery("allAnswersByQuestionId", AnswerEntity.class).setParameter("questionId", questionId).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }


    /**
     * Delete answer by answer Id
     *
     * @param answerEntity
     */
    public void deleteAnswer(AnswerEntity answerEntity) {
        entityManager.remove(answerEntity);
    }

}
