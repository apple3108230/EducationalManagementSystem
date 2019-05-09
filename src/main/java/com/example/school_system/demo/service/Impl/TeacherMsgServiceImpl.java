package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.TeacherMsgDao;
import com.example.school_system.demo.pojo.TeacherMsg;
import com.example.school_system.demo.service.TeacherMsgSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherMsgServiceImpl implements TeacherMsgSerivce {

    @Autowired
    private TeacherMsgDao teacherMsgDao;

    @Override
    public boolean insertBatchTeacherMsg(List<TeacherMsg> teacherMsgList) {
        int result=teacherMsgDao.insertBatchTeacherMsg(teacherMsgList);
        if (result>0){
            return true;
        }
        return false;
    }

    @Override
    public String getLastTeacherId(String academyId) {
        return teacherMsgDao.getLastTeacherId(academyId);
    }
}
