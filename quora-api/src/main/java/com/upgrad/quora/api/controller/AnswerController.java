package com.upgrad.quora.api.controller;

import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@RestController
public class AnswerController {

    @Autowired
    private QuestionService questionService;


    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> create(@RequestHeader("authorization") final String authorization, @RequestParam(name = "question_id") final Integer questionId, final AnswerRequest answerRequest) throws AuthenticationFailedException, InvalidQuestionException {

        final QuestionEntity questionEntity = questionService.getQuestionByQuestionId(questionId);
        final AnswerEntity answerEntity = new AnswerEntity();

        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setQuestionId(questionEntity);
        //answerEntity.setUserId();

        return new ResponseEntity<AnswerResponse>(HttpStatus.CREATED);
    }
}
