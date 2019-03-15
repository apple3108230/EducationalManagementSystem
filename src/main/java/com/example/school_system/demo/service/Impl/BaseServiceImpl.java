package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.BaseDao;
import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseServiceImpl implements BaseService {

    @Autowired
    private BaseDao baseDao;

    @Override
    public User getUserByName(String username) {
        return baseDao.getUserByName(username);
    }

    @Override
    public void resetPwdByName(String newPwd, String salt,String username) {
       baseDao.resetPwdByName(newPwd,salt,username);
    }
}
