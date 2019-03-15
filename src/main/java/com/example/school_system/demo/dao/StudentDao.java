package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Student;
import com.example.school_system.demo.pojo.Student_status_msg;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface StudentDao {
    public Student getStudentById(String id);
    public void updateInfoById(@Param("map") Map map);
    public Student_status_msg getStudentStatusMsgId(String id);
}
