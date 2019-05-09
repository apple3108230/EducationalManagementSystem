package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.TeacherMsg;

import java.util.List;

public interface TeacherMsgSerivce {
    public boolean insertBatchTeacherMsg(List<TeacherMsg> teacherMsgList);
    public String getLastTeacherId(String academyId);
}
