package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.controller.TeacherController;
import com.example.school_system.demo.dao.TeacherDao;
import com.example.school_system.demo.pojo.DeleteScoreInfo;
import com.example.school_system.demo.pojo.StudentScore;
import com.example.school_system.demo.pojo.TeacherMsg;
import com.example.school_system.demo.service.TeacherService;
import com.example.school_system.demo.utils.ScoreUtil;
import com.example.school_system.demo.utils.StringUtil;
import com.example.school_system.demo.utils.WebUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherDao teacherDao;



    @Override
    public TeacherMsg getTeacherMsgById(String id) {
        return teacherDao.getTeacherMsgById(id);
    }

    @Override
    public boolean insertScoreByStudentId(List<StudentScore> scoreList) {
        int result=teacherDao.insertScoreByStudentId(scoreList);
        if(result>0){
            return true;
        }
        return false;
    }

    @Override
    public boolean updateStudentScoreByCourseAndStudentId(StudentScore studentScore) {
        int result=teacherDao.updateStudentScoreByCourseAndStudentId(studentScore);
        if(result>=0){
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteStudentScoreByCourseAndStudentId(DeleteScoreInfo deleteScoreInfo) {
        int result=teacherDao.deleteStudentScoreByCourseAndStudentId(deleteScoreInfo);
        if(result>=0){
            return true;
        }
        return false;
    }

}
