package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserBusinessService userBusinessService;

    @Autowired
    private AuthenticationService authenticationService;

    @RequestMapping(method = RequestMethod.POST, path = "/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> signup(final SignupUserRequest signupUserRequest) {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUsername(signupUserRequest.getUserName());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutme(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setRole("nonadmin");
        userEntity.setContactNumber(signupUserRequest.getContactNumber());

        final UserEntity createdUserEntity;
        try {
            createdUserEntity = userBusinessService.signup(userEntity);
        } catch (SignUpRestrictedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.CONFLICT);
        }

        SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> signin(@RequestHeader("authorization") final String authorization) {

        String credentials = authorization.split("Basic ")[1];
        String[] credentialsArray = credentials.split(":");

        UserAuthEntity userAuthEntity;
        try {
            userAuthEntity = authenticationService.authenticateForSignin(credentialsArray[0], credentialsArray[1]);
        } catch (AuthenticationFailedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        UserEntity userEntity = userAuthEntity.getUserId();

        SigninResponse signinResponse = new SigninResponse().id(userEntity.getUuid())
                .message("SIGNED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", userAuthEntity.getAccessToken());
        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> signout(@RequestHeader("authorization") final String authorization) {

        String accessToken = authorization;

        UserAuthEntity userAuthEntity;
        try {
            userAuthEntity = authenticationService.authenticateForSignout(accessToken);
        } catch (SignOutRestrictedException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        UserEntity user = userAuthEntity.getUserId();

        SignoutResponse signoutResponse = new SignoutResponse().id(user.getUuid())
                .message("SIGNED OUT SUCCESSFULLY");
        return new ResponseEntity<SignoutResponse>(signoutResponse, HttpStatus.OK);
    }
}
