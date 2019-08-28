package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseDao {
    public User getUserByUserName(String username);
    public int resetPwdByUserName(@Param("newPwd") String newPwd,@Param("salt") String salt,@Param("username") String username);
    public int insertEmailByUserName(@Param("username") String username,@Param("email") String email);
    public int insertUser(User user);
}
