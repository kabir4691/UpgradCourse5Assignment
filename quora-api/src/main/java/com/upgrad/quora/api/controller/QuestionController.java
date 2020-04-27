package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
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
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> create(@RequestHeader("authorization") final String authorization, final QuestionRequest questionRequest) {

        String accessToken = authorization;

        UserAuthEntity userAuthEntity;
        try {
            userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Build question entity
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());
        questionEntity.setUserId(userAuthEntity.getUserId());

        final QuestionEntity questionPosted = questionService.submitQuestion(questionEntity);
        QuestionResponse questionResponse = new QuestionResponse().id(questionPosted.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getAll(@RequestHeader("authorization") final String authorization) {

        String accessToken = authorization;

        try {
            authenticationService.authorizeUserLogedin(accessToken);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        final List<QuestionEntity> allQuestions = questionService.getAllQuestions();

        List<QuestionDetailsResponse> questionResponse = new ArrayList<QuestionDetailsResponse>();
        for (QuestionEntity entity : allQuestions) {
            QuestionDetailsResponse response = new QuestionDetailsResponse();
            response.setId(entity.getUuid());
            response.setContent(entity.getContent());
            questionResponse.add(response);
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(questionResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/edit/{questionId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> edit(@RequestHeader("authorization") final String authorization, @RequestParam(name = "questionId") final String questionId, final QuestionEditRequest questionEditRequest) {
        String accessToken = authorization;

        UserAuthEntity userAuthEntity;
        try {
            userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Checking the ownership of the question
        try {
            if (!questionService.isUserOwnerOfTheQuestrion(questionId, userAuthEntity.getUserId().getUuid())) {
                AuthenticationFailedException e = new AuthenticationFailedException("ATHR-003", "Only the question owner can edit the question");
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
                return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
            }
        } catch (InvalidQuestionException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        QuestionEntity updateQuestionEntity;
        try {
            updateQuestionEntity = questionService.getQuestionByQuestionId(questionId);
        } catch (InvalidQuestionException e) {
            InvalidQuestionException e2 = new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e2.getCode()).message(e2.getErrorMessage()).rootCause(e2.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        updateQuestionEntity.setDate(ZonedDateTime.now());
        updateQuestionEntity.setContent(questionEditRequest.getContent());

        final QuestionEntity questionEdited = questionService.submitQuestion(updateQuestionEntity);
        QuestionEditResponse questionEditedResponse = new QuestionEditResponse().id(questionEdited.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditedResponse, HttpStatus.CREATED);

    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> delete(@RequestHeader("authorization") final String authorization, @RequestParam(name = "questionId") final String questionId) {

        String accessToken = authorization;

        UserAuthEntity userAuthEntity;
        try {
            userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Checking the ownership of the question
        try {
            if (!questionService.isUserOwnerOfTheQuestrion(questionId, userAuthEntity.getUserId().getUuid())) {
                AuthenticationFailedException e = new AuthenticationFailedException("ATHR-003", "Only the question owner can delete the question");
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
                return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
            }

            // Delete question
            questionService.deleteQuestion(questionId);
        } catch (InvalidQuestionException e) {
            InvalidQuestionException e2 = new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e2.getCode()).message(e2.getErrorMessage()).rootCause(e2.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse().id(questionId).status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/all/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getAllByUser(@RequestHeader("authorization") final String authorization, @RequestParam(name = "userId") final String userId) {


        String accessToken = authorization;

        try {
            authenticationService.authorizeUserLogedin(accessToken);
        } catch (AuthorizationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }

        final List<QuestionEntity> userQuestions = questionService.getQuestionsByUser(userId);

        List<QuestionDetailsResponse> questionResponse = new ArrayList<QuestionDetailsResponse>();
        for (QuestionEntity entity : userQuestions) {
            QuestionDetailsResponse response = new QuestionDetailsResponse();
            response.setId(entity.getUuid());
            response.setContent(entity.getContent());
            questionResponse.add(response);
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(questionResponse, HttpStatus.OK);
    }


}
