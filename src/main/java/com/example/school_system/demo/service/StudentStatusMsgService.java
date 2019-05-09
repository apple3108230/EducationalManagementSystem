package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.StudentStatusMsg;

import java.util.List;

public interface StudentStatusMsgService {
    public boolean insertBatchStudentStatusMsg(List<StudentStatusMsg> studentStatusMsgList);
}
