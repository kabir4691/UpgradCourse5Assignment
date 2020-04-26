package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDAO;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionDAO questionDAO;


    /**
     * Returns QuestionEntity by question ID
     *
     * @param questionId
     * @return QuestionEntity
     */
    public QuestionEntity getQuestionByQuestionId(String questionId) throws InvalidQuestionException {
        QuestionEntity questionEntity = questionDAO.getQuestionByQuestionId(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        return questionEntity;
    }

    /**
     * Return true if question is already exist
     *
     * @param questionId
     * @return boolean
     * @throws InvalidQuestionException
     */
    public boolean isQuestionExist(String questionId) {
        return questionDAO.getQuestionByQuestionId(questionId) != null;
    }

    /**
     * Submits user question
     *
     * @param questionEntity
     * @return QuestionEntity
     */
    public QuestionEntity submitQuestion(QuestionEntity questionEntity) {
        return questionDAO.submitQuestion(questionEntity);
    }

    /**
     * Get all questions
     *
     * @return List<QuestionEntity>
     */
    public List<QuestionEntity> getAllQuestions() {
        return questionDAO.getAllQuestions();
    }

    /**
     * Return true if user owns the question else false
     *
     * @param questionId
     * @param userId
     * @return boolean
     * @throws InvalidQuestionException
     */
    public boolean isUserOwnerOfTheQuestrion(String questionId, String userId) throws InvalidQuestionException {
        return getQuestionByQuestionId(questionId).getUserId().getUuid().equalsIgnoreCase(userId);
    }


    public void deleteQuestion(String questionId) throws InvalidQuestionException {
        // Checking question is existence
        if (!isQuestionExist(questionId)) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        questionDAO.deleteQuestion(questionId);
    }
}
