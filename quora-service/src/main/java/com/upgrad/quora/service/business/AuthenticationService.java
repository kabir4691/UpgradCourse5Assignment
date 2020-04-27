package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
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
    public UserAuthEntity authenticateForSignout(final String accessToken) throws AuthenticationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuth(accessToken);
        if (userAuthEntity == null) {
            throw new AuthenticationFailedException("SGR-001", "User is not Signed in");
        }

        UserEntity userEntity = userAuthEntity.getUserId();
        userAuthEntity.setLogoutAt(ZonedDateTime.now());
        userDao.updateUser(userEntity);

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
