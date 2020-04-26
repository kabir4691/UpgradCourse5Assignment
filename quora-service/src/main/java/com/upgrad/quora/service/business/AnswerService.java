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
     * Creates new Answer
     *
     * @param answerEntity
     */
    public void submitAnswer(AnswerEntity answerEntity) {
        answerDAO.submitAnswer(answerEntity);
    }

    /**
     * Updates answer if there is a existing answer
     *
     * @param answerId
     * @throws AnswerNotFoundException
     */
    public void updateAnswer(Integer answerId) throws AnswerNotFoundException {
        AnswerEntity answer = getAnswer(answerId);
        // Existing answer available, update it
        answerDAO.submitAnswer(answer);
    }

    /**
     * Returns answer by answerId
     *
     * @param answerId
     * @return
     */
    public AnswerEntity getAnswer(Integer answerId) throws AnswerNotFoundException {
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
    public void deleteAnswer(Integer answerId) throws AnswerNotFoundException {
        AnswerEntity answer = answerDAO.getAnswer(answerId);
        if (answer == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        // There is an existing answer, deleting ita
        answerDAO.deleteAnswer(answerId);
    }
}
