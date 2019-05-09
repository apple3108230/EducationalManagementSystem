package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.User;

public interface BaseService {
    public User getUserByUserName(String username);
    public int resetPwdByUserName(String newPwd,String salt,String username);
    public int insertEmailByUserName(String username,String email);
    public boolean insertUser(User user);
}
