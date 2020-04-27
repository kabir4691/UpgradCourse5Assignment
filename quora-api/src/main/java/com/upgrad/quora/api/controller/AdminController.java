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

    //This method is a Controller method to delete the User
    @RequestMapping(method = RequestMethod.DELETE, path = "/admin/user/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> userDelete(@PathVariable("userId") final String userUuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        ErrorResponse errorResponse = new ErrorResponse();

        try{
            String accessToken = authorization;

            //Invoking Authentication Business service file to check for authenticity for User delete operation
            authenticationService.authenticateForUserDelete(accessToken);

            //Invoking User Admin Business service file to retrieve the User Entity using user ID
            final UserEntity userEntity = userAdminService.getUserByID(userUuid);
            UserDeleteResponse userDeleteResponse = new UserDeleteResponse().id(userEntity.getUuid())
                    .status("USER SUCCESSFULLY DELETED");

            //Operation to delete the user
            userAdminService.deleteUser(userEntity);
            return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);
        } catch(UserNotFoundException e){
            //Wrapping a ResponseEntity with the User Not found Error Response
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        } catch(AuthorizationFailedException e){
            //Wrapping a ResponseEntity with the Authorization Failed or Forbidden Error Response
            errorResponse.code(e.getCode()).message(e.getErrorMessage()).rootCause(e.getErrorMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (Exception e){
            //Wrapping a ResponseEntity with the generic Bad request Error Response for unknown errors
            errorResponse.code("400").message("Bad Request").rootCause("Bad Request");
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}