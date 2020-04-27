package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    @Autowired
    private UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity getUserByID(final String userUuid) throws UserNotFoundException {
        UserEntity signedInuser = userDao.getUserByID(userUuid);
        if (signedInuser == null) {
            //UserNotFoundException is thrown if the user with uuid whose profile is to be retrieved does not exist in the database.
            throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
        }
        return signedInuser;
    }
}
