package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.StudentPersonalMessageDao;
import com.example.school_system.demo.pojo.Student;
import com.example.school_system.demo.service.StudentPersonalMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentPersonalMsgServiceImpl implements StudentPersonalMsgService {

    @Autowired
    private StudentPersonalMessageDao studentPersonalMessageDao;

    @Override
    public boolean insertBatchStudentPersonalMessage(List<Student> studentList) {
        int result=studentPersonalMessageDao.insertBatchStudentPersonalMessage(studentList);
        if(result>0){
            return true;
        }
        return false;
    }

    @Override
    public String getLastStudentIdByClassId(String classId) {
        return studentPersonalMessageDao.getLastStudentIdByClassId(classId);
    }

}
