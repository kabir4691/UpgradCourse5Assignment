package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDAO;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionDAO questionDAO;

    @Autowired
    private UserDao userDao;

    /**
     * Returns QuestionEntity by question ID
     *
     * @param questionId
     * @return QuestionEntity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity getQuestionByQuestionId(String questionId, String actionType) throws InvalidQuestionException {
        QuestionEntity questionEntity = questionDAO.getQuestionByQuestionId(questionId);
        if (questionEntity == null) {
            if(actionType.equals("QUESTION"))
                throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
            else if(actionType.equals("ANSWER"))
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
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean isQuestionExist(String questionId) {
        return questionDAO.getQuestionByQuestionId(questionId) != null;
    }

    /**
     * Submits user question
     *
     * @param questionEntity
     * @return QuestionEntity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity submitQuestion(QuestionEntity questionEntity) {
        return questionDAO.submitQuestion(questionEntity);
    }

    /**
     * Get all questions
     *
     * @return List<QuestionEntity>
     */
    @Transactional(propagation = Propagation.REQUIRED)
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
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean isUserOwnerOfTheQuestion(String questionId, String userId) throws InvalidQuestionException {
        return getQuestionByQuestionId(questionId, "QUESTION").getUserId().getUuid().equalsIgnoreCase(userId);
    }


    /**
     * Delete question
     *
     * @param questionId
     * @throws InvalidQuestionException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteQuestion(String questionId) throws InvalidQuestionException {
        // Checking question is existence
        if (!isQuestionExist(questionId)) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        QuestionEntity questionEntity = getQuestionByQuestionId(questionId,"QUESTION");
        questionDAO.deleteQuestion(questionEntity);
    }

    /**
     * Get user question
     *
     * @param userId
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<QuestionEntity> getQuestionsByUser(String userId) {
        return questionDAO.getQuestionsByUser(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void getUserByID(final String userUuid) throws UserNotFoundException {
        UserEntity signedInuser = userDao.getUserByID(userUuid);
        if (signedInuser == null) {
            //UserNotFoundException is thrown if the user with uuid whose profile is to be deleted does not exist in the database
            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
    }
}
