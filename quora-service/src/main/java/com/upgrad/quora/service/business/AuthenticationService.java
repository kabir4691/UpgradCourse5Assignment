package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {

    @Autowired
    private UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticateForSignin(final String username, final String password) throws AuthenticationFailedException {
        UserEntity userEntity = userDao.getUserByUsername(username);
        if (userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }

        final String encryptedPassword = PasswordCryptographyProvider.encrypt(password, userEntity.getSalt());
        if (!(encryptedPassword.equals(userEntity.getPassword()))) {
            throw new AuthenticationFailedException("ATH-002", "Password Failed");
        }

        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
        UserAuthEntity userAuth = new UserAuthEntity();
        userAuth.setUuid(UUID.randomUUID().toString());
        userAuth.setUserId(userEntity);
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime expiresAt = now.plusHours(8);
        userAuth.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
        userAuth.setLoginAt(now);
        userAuth.setExpiresAt(expiresAt);

        userDao.createUserAuth(userAuth);

        return userAuth;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticateForSignout(final String accessToken) throws SignOutRestrictedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuth(accessToken);
        if (userAuthEntity == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }

        UserEntity userEntity = userAuthEntity.getUserId();
        userAuthEntity.setLogoutAt(ZonedDateTime.now());
        userDao.updateUser(userEntity);

        return userAuthEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticateForUserProfile(final String accessToken) throws AuthorizationFailedException {
        //Invoking the DAO layer to get the User Auth Entity details by using Access token information
        UserAuthEntity userAuthEntity = userDao.getUserAuth(accessToken);
        if (userAuthEntity == null) {
            //AuthorizationFailedException is thrown if the access token provided by the user does not exist in the database
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLoginAt() != null && userAuthEntity.getLogoutAt().isAfter(userAuthEntity.getLoginAt())){
            //AuthorizationFailedException is thrown if the user has signed out
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
        }
        return userAuthEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticateForUserDelete(final String accessToken) throws AuthorizationFailedException {
        //Invoking the DAO layer to get the User Auth Entity details by using Access token information
        UserAuthEntity userAuthEntity = userDao.getUserAuth(accessToken);
        if (userAuthEntity == null) {
            //AuthorizationFailedException is thrown if the access token provided by the user does not exist in the database
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLoginAt() != null && userAuthEntity.getLogoutAt().isAfter(userAuthEntity.getLoginAt())){
            //AuthorizationFailedException is thrown if the user has signed out
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        }

        UserEntity userEntity = userAuthEntity.getUserId();
        if(userEntity.getRole().equalsIgnoreCase("nonadmin")){
            //AuthorizationFailedException is thrown if the role of the user is 'nonadmin'
            throw new AuthorizationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
        }
        return userAuthEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authorizeUserLoggedin(final String accessToken, String operationType, String actionType) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuth(accessToken);
        // Authorize user login
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not Signed in");
        }
        // Authorize user session
        if (userAuthEntity.getLogoutAt() != null && userAuthEntity.getLoginAt() != null && userAuthEntity.getLogoutAt().isAfter(userAuthEntity.getLoginAt())) {
            if(actionType.equals("QUESTION")){
                if(operationType.equals("CREATE"))
                    throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");
                else if(operationType.equals("EDIT"))
                    throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit the question");
                else if(operationType.equals("DELETE"))
                    throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete a question");
                else if(operationType.equals("GET"))
                    throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get all questions posted by a specific user");
            } else if(actionType.equals("ANSWER")){
                if(operationType.equals("CREATE"))
                    throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post an answer");
                else if(operationType.equals("EDIT"))
                    throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit an answer");
                else if(operationType.equals("DELETE"))
                    throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete an answer");
                else if(operationType.equals("GET"))
                    throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get the answers");
            }
        }
        return userAuthEntity;
    }

}
