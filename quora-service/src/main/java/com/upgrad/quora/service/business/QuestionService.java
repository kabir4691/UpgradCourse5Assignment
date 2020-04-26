package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDAO;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public QuestionEntity getQuestionByQuestionId(Integer questionId) throws InvalidQuestionException {
        QuestionEntity questionEntity = questionDAO.getQuestionByQuestionId(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        return questionEntity;
    }
}
