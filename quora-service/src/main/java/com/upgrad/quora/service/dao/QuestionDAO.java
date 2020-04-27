package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDAO {

    @PersistenceContext
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserDao userDao;

    /**
     * Returns Question details by questionId
     *
     * @param questionId
     * @return QuestionEntity
     */
    public QuestionEntity getQuestionByQuestionId(String questionId) {
        try {
            return entityManager.createNamedQuery("questionByQuestionId", QuestionEntity.class).setParameter("questionId", questionId).getSingleResult();
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
     * Get user questions
     *
     * @param uuid
     * @return List<QuestionEntity>
     */
    public List<QuestionEntity> getQuestionsByUser(String uuid) {

        final UserEntity userEntity = userDao.getUserByUUID(uuid);
        try {
            return entityManager.createNamedQuery("questionByUserId", QuestionEntity.class).setParameter("userId", userEntity.getId()).getResultList();
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
        entityManager.merge(questionEntity);
        return questionEntity;
    }

    /**
     * Delete question
     *
     * @param questionEntity
     */
    public void deleteQuestion(QuestionEntity questionEntity) {
        entityManager.remove(questionEntity);
    }
}
