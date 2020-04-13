package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAdminService {

    @Autowired
    private UserDao userDao;

    /*public UserEntity getUser(final String userUuid) {
        return userDao.getUser(userUuid);
    }*/
}
