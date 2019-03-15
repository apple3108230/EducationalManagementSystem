package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.StudentDao;
import com.example.school_system.demo.pojo.Student;
import com.example.school_system.demo.pojo.Student_status_msg;
import com.example.school_system.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentDao studentDao;

    @Override
    public Student getStudentById(String id) {
        return studentDao.getStudentById(id);
    }

    @Override
    public void updateInfoById(Map map) {
        studentDao.updateInfoById(map);
    }

    @Override
    public Student_status_msg getStudentStatusMsgId(String id) {
        return studentDao.getStudentStatusMsgId(id);
    }
}
