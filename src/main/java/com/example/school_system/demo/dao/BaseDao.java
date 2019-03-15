package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseDao {
    public User getUserByName(String username);
    public void resetPwdByName(@Param("newPwd") String newPwd,@Param("salt") String salt,@Param("username") String username);
}
