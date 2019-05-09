package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.StudentStatusMsgDao;
import com.example.school_system.demo.pojo.StudentStatusMsg;
import com.example.school_system.demo.service.AcademyService;
import com.example.school_system.demo.service.StudentStatusMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentStatusMsgServiceImpl implements StudentStatusMsgService {

    @Autowired
    private StudentStatusMsgDao studentStatusMsgDao;

    @Override
    public boolean insertBatchStudentStatusMsg(List<StudentStatusMsg> studentStatusMsgList) {
        int result=studentStatusMsgDao.insertBatchStudentStatusMsg(studentStatusMsgList);
        if(result>0){
            return true;
        }
        return false;
    }

}
