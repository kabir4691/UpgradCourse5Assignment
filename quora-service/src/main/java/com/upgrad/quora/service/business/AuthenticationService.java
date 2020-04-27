package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
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
    public UserAuthEntity authorizeUserLogedin(final String accessToken) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuth(accessToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("SGR-001", "User is not Signed in");
        }
        return userAuthEntity;
    }

}
