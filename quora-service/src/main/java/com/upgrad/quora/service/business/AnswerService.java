package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDAO;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private AnswerDAO answerDAO;

    @Autowired
    private QuestionService questionService;

    /**
     * Submits user posted answer
     *
     * @param answerEntity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity submitAnswer(AnswerEntity answerEntity) {
        return answerDAO.submitAnswer(answerEntity);
    }

    /**
     * Returns answer by answerId
     *
     * @param answerId
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity getAnswer(String answerId) throws AnswerNotFoundException {
        AnswerEntity answer = answerDAO.getAnswer(answerId);
        if (answer == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        return answer;
    }

    /**
     * Delete answer by answerId
     *
     * @param answerId
     * @throws AnswerNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAnswer(String answerId) throws AnswerNotFoundException {
        AnswerEntity answer = getAnswer(answerId);
        answerDAO.deleteAnswer(answer);
    }

    /**
     * Returns true if user is the owner of the answer
     *
     * @param answerId
     * @param userId
     * @return boolean
     * @throws AnswerNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean isUserOwnerOfTheAnswer(String answerId, String userId) throws AnswerNotFoundException {
        return getAnswer(answerId).getUserId().getUuid().equals(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<AnswerEntity> getAllAnswers(String questionId) throws InvalidQuestionException {
        QuestionEntity questionEntity = questionService.getQuestionByQuestionId(questionId, "ANSWER");
        return answerDAO.getAllAnswers(questionEntity.getId());
    }
}
