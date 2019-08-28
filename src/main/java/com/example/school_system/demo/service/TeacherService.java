package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.DeleteScoreInfo;
import com.example.school_system.demo.pojo.StudentScore;
import com.example.school_system.demo.pojo.TeacherMsg;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.List;

public interface TeacherService {
    public TeacherMsg getTeacherMsgById(String id);
}
