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
    public User getUserByUserName(String username) {
        return baseDao.getUserByUserName(username);
    }

    @Override
    public int resetPwdByUserName(String newPwd, String salt,String username) {
        return baseDao.resetPwdByUserName(newPwd,salt,username);
    }

    @Override
    public int insertEmailByUserName(String username, String email) {
        return baseDao.insertEmailByUserName(username, email);
    }

    @Override
    public boolean insertUser(User user) {
        int result=baseDao.insertUser(user);
        if(result>0){
            return true;
        }
        return false;
    }
}
