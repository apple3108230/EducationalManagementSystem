package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.Student;
import com.example.school_system.demo.pojo.Student_status_msg;

import java.util.Map;

public interface StudentService {
    public Student getStudentById(String id);
    public void updateInfoById(Map map);
    public Student_status_msg getStudentStatusMsgId(String id);
}
