package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDAO;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnswerService {

    @Autowired
    private AnswerDAO answerDAO;

    /**
     * Submits user posted answer
     *
     * @param answerEntity
     */
    public AnswerEntity submitAnswer(AnswerEntity answerEntity) {
        return answerDAO.submitAnswer(answerEntity);
    }

    /**
     * Returns answer by answerId
     *
     * @param answerId
     * @return
     */
    public AnswerEntity getAnswer(String answerId) throws AnswerNotFoundException {
        AnswerEntity answer = answerDAO.getAnswer(answerId);
        if (answer == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        return answer;
    }

    public boolean isAnswerExist(String answerId) throws AnswerNotFoundException {
        return getAnswer(answerId) != null;
    }

    /**
     * Delete answer by answerId
     *
     * @param answerId
     * @throws AnswerNotFoundException
     */
    public void deleteAnswer(String answerId) throws AnswerNotFoundException {
        if (isAnswerExist(answerId)) {
            // There is an existing answer, deleting it
            answerDAO.deleteAnswer(answerId);
        }
    }

    /**
     * Returns true if user is the owner of the answer
     *
     * @param answerId
     * @param userId
     * @return boolean
     * @throws AnswerNotFoundException
     */
    public boolean isUserOwnerOfTheAnswer(String answerId, String userId) throws AnswerNotFoundException {
        AnswerEntity answer = getAnswer(answerId);
        return answer.getUserId().getUuid().equals(userId);
    }
}
