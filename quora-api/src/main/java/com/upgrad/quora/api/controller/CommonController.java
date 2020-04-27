package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.ErrorResponse;
import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.UserProfileService;
import com.upgrad.quora.service.entity.UserAuthEntity;
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
public class CommonController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private AuthenticationService authenticationService;

    //This method is a Controller method to get the User information
    @RequestMapping(method = RequestMethod.GET, path = "/userprofile/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getUser(@PathVariable("userId") final String userUuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {

        ErrorResponse errorResponse = new ErrorResponse();

        try{
            String accessToken = authorization;
            //Invoking Authentication Business service file to check for authenticity to retrieve User profile operation
            authenticationService.authenticateForUserProfile(accessToken);

            //Invoking User Admin Business service file to retrieve the User Entity using user ID
            final UserEntity userEntity = userProfileService.getUserByID(userUuid);

            //Operation to get the user information
            UserDetailsResponse userDetailsResponse = new UserDetailsResponse().firstName(userEntity.getFirstName())
                    .lastName(userEntity.getLastName()).userName(userEntity.getUsername()).emailAddress(userEntity.getEmail())
                    .country(userEntity.getCountry()).aboutMe(userEntity.getAboutme()).dob(userEntity.getDob()).contactNumber(userEntity.getContactNumber());

            return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
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