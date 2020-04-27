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
import java.util.UUID;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> create(@RequestHeader("authorization") final String authorization, final QuestionRequest questionRequest) throws AuthenticationFailedException, AuthorizationFailedException {

        String accessToken = authorization.split("Bearer")[1];

        // Authorize user login
        UserAuthEntity userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);

        // Authorize user session
        if (userAuthEntity.getLogoutAt() != null && userAuthEntity.getLoginAt() != null && userAuthEntity.getLogoutAt().isAfter(userAuthEntity.getLoginAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post an question");
        }

        // Build question entity
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());
        //questionEntity.setUserId(userAuthEntity.getUserId());

        final QuestionEntity questionPosted = questionService.submitQuestion(questionEntity);
        QuestionResponse questionResponse = new QuestionResponse().id(questionPosted.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }
/*
    @RequestMapping(method = RequestMethod.GET, path = "/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDetailsResponse> getAll(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {

        String accessToken = authorization.split("Bearer")[1];
        // Authorize user login
        UserAuthEntity userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);

         // Authorize user session
        if (userAuthEntity.getLogoutAt() != null && userAuthEntity.getLoginAt() != null && userAuthEntity.getLogoutAt().isAfter(userAuthEntity.getLoginAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get the answers");
        }

        final List<QuestionEntity> allQuestions = questionService.getAllQuestions();

        QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();

        // TODO: Returns all questions

    }
    */


    @RequestMapping(method = RequestMethod.PUT, path = "/edit/{questionId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> edit(@RequestHeader("authorization") final String authorization, @RequestParam(name = "questionId") final String questionId, final QuestionEditRequest questionEditRequest) throws AuthenticationFailedException, InvalidQuestionException, AuthorizationFailedException {
        String accessToken = authorization.split("Bearer")[1];
        // Authorize user login
        UserAuthEntity userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);

        // Authorize user session
        if (userAuthEntity.getLogoutAt() != null && userAuthEntity.getLoginAt() != null && userAuthEntity.getLogoutAt().isAfter(userAuthEntity.getLoginAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit the question");
        }

        // Checking the ownership of the question
        if (!questionService.isUserOwnerOfTheQuestrion(questionId, userAuthEntity.getUserId().getUuid())) {
            throw new AuthenticationFailedException("ATHR-003", "Only the question owner can edit the question");
        }

        // Checking question is existence
        if (!questionService.isQuestionExist(questionId)) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }

        QuestionEntity updateQuestionEntity = questionService.getQuestionByQuestionId(questionId);
        updateQuestionEntity.setDate(ZonedDateTime.now());
        updateQuestionEntity.setContent(questionEditRequest.getContent());

        final QuestionEntity questionEdited = questionService.submitQuestion(updateQuestionEntity);
        QuestionEditResponse questionEditedResponse = new QuestionEditResponse().id(questionEdited.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditedResponse, HttpStatus.CREATED);

    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/delete", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> delete(@RequestHeader("authorization") final String authorization, @RequestParam(name = "questionId") final String questionId) throws AuthenticationFailedException, InvalidQuestionException, AuthorizationFailedException {

        String accessToken = authorization.split("Bearer")[1];
        // Authorize user login
        UserAuthEntity userAuthEntity = authenticationService.authorizeUserLogedin(accessToken);

        // Authorize user session
        if (userAuthEntity.getLogoutAt() != null && userAuthEntity.getLoginAt() != null && userAuthEntity.getLogoutAt().isAfter(userAuthEntity.getLoginAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete the question");
        }

        // Checking the ownership of the question
        if (!questionService.isUserOwnerOfTheQuestrion(questionId, userAuthEntity.getUserId().getUuid())) {
            throw new AuthenticationFailedException("ATHR-003", "Only the question owner can delete the question");
        }

        // Delete question
        questionService.deleteQuestion(questionId);
        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse().id(questionId).status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);
    }

}
