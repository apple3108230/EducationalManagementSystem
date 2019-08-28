package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.StudentDao;
import com.example.school_system.demo.dao.StudentScoreDao;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.CourseSelectionService;
import com.example.school_system.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.*;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentDao studentDao;
    @Autowired
    private StudentScoreDao studentScoreDao;


    @Override
    public Student getStudentById(String id) {
        return studentDao.getStudentById(id);
    }

    @Override
    public void updateInfoById(Map map) {
        studentDao.updateInfoById(map);
    }

    @Override
    public StudentStatusMsg getStudentStatusMsgId(String id) {
        return studentDao.getStudentStatusMsgId(id);
    }

    @Override
    public List<Timestable> getTimestableByStudentClass(String studentClass,String term) {
        return studentDao.getTimestableByStudentClass(studentClass,term);
    }

    @Override
    public List<Course> getCourseByMajorName(String majorName) {
        return studentDao.getCourseByMajorName(majorName);
    }

    @Override
    public StudentScore getStudentScore(String term,String id) {
        Map<String,String> conditionMap=new HashMap<>();
        conditionMap.put("term", term);
        conditionMap.put("student_id",id);
        return studentScoreDao.selectStudentScoresByCondition(conditionMap).get(0);
    }

}
