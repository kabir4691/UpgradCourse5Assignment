package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class AnswerController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AuthenticationService authenticationService;


    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> create(@RequestHeader("authorization") final String authorization, @RequestParam(name = "question_id") final String questionId, final AnswerRequest answerRequest) {

        String accessToken = authorization.split("Bearer ")[1];

        UserAuthEntity userAuthEntity;
        try {
            userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate existence of question
        final QuestionEntity questionEntity;
        try {
            questionEntity = questionService.getQuestionByQuestionId(questionId);
        } catch (InvalidQuestionException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        //Build answer entity
        final AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setQuestionId(questionEntity);
        answerEntity.setUserId(userAuthEntity.getUserId());

        final AnswerEntity answerPosted = answerService.submitAnswer(answerEntity);
        AnswerResponse answerResponse = new AnswerResponse().id(answerPosted.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> update(@RequestHeader("authorization") final String authorization, @RequestParam(name = "answerId") final String answerId, final AnswerEditRequest answerEditRequest)  {

        String accessToken = authorization.split("Bearer ")[1];

        UserAuthEntity userAuthEntity;
        try {
            userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate the ownership of the answer
        try {
            if (!answerService.isUserOwnerOfTheAnswer(answerId, userAuthEntity.getUserId().getUuid())) {
                Exception e = new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
                return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
            }

            // Construct updated answer
            final AnswerEntity answer = answerService.getAnswer(answerId);
            answer.setDate(ZonedDateTime.now());
            answer.setAns(answerEditRequest.getContent());

            final AnswerEntity answerPosted = answerService.submitAnswer(answer);
            AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(answerPosted.getUuid()).status("ANSWER EDITED");
            return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
        } catch (AnswerNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> delete(@RequestHeader("authorization") final String authorization, @RequestParam(name = "answerId") final String answerId) {

        String accessToken = authorization.split("Bearer ")[1];

        UserAuthEntity userAuthEntity;
        try {
            userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Validate the ownership of the answer
        try {
            if (!answerService.isUserOwnerOfTheAnswer(answerId, userAuthEntity.getUserId().getUuid())) {
                Exception e = new AuthenticationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
                return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
            }

            // Delete the answer
            answerService.deleteAnswer(answerId);
        } catch (AnswerNotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(answerId).status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswers(@RequestHeader("authorization") final String authorization, @RequestParam(name = "questionId") final String questionId) {

        String accessToken = authorization.split("Bearer ")[1];

        try {
            authenticationService.authorizeUserLogedin(accessToken);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        final QuestionEntity questionEntity;
        List<AnswerEntity> allAnswers;
        try {
            questionEntity = questionService.getQuestionByQuestionId(questionId);
            allAnswers = answerService.getAllAnswers(questionId);
        } catch (InvalidQuestionException e) {
            Exception e2 = new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e2.getCode()).message(e2.getErrorMessage()).rootCause(e2.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        List<AnswerDetailsResponse> answerResponse = new ArrayList<AnswerDetailsResponse>();
        for (AnswerEntity answer : allAnswers) {
            AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse();
            answerDetailsResponse.setAnswerContent(answer.getAns());
            answerDetailsResponse.setQuestionContent(questionEntity.getContent());
            answerDetailsResponse.setId(answer.getUuid());
            answerResponse.add(answerDetailsResponse);
        }
        return new ResponseEntity<List<AnswerDetailsResponse>>(answerResponse, HttpStatus.OK);
    }
}
