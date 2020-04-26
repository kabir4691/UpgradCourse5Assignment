package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.ErrorResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.UserAdminService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AdminController {

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private AuthenticationService authenticationService;

    @RequestMapping(method = RequestMethod.DELETE, path = "/admin/user/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> userDelete(@PathVariable("userId") final String userUuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        ErrorResponse errorResponse = new ErrorResponse();

        try{
            String accessToken = authorization.split("Bearer ")[1];
            authenticationService.authenticateForUserDelete(accessToken);

            final UserEntity userEntity = userAdminService.getUserByID(userUuid);
            UserDeleteResponse userDeleteResponse = new UserDeleteResponse().id(userEntity.getUuid())
                    .status("USER SUCCESSFULLY DELETED");

            userAdminService.deleteUser(userEntity);
            return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);
        } catch(UserNotFoundException e){
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        } catch(AuthorizationFailedException e){
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (Exception e){
            errorResponse.code("400").message("Bad Request").rootCause("Bad Request");
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}