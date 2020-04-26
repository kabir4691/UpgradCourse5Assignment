package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionRequest;
import com.upgrad.quora.api.model.QuestionResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> create(@RequestHeader("authorization") final String authorization, final QuestionRequest questionRequest) throws AuthenticationFailedException {

        String accessToken = authorization.split("Bearer")[1];

        // Get Currently logged in user
        final UserAuthEntity userAuthEntity = authService.getUserAuthEntity(accessToken);

        //TODO: Add validation "User is signed out.Sign in first to create an question

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
}
