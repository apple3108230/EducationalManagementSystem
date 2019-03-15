package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.User;

public interface BaseService {
    public User getUserByName(String username);
    public void resetPwdByName(String newPwd,String salt,String username);
}
