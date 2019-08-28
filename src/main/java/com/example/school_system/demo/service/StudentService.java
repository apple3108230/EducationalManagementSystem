package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.*;

import java.util.List;
import java.util.Map;

public interface StudentService {
    public Student getStudentById(String id);
    public void updateInfoById(Map map);
    public StudentStatusMsg getStudentStatusMsgId(String id);
    public List<Timestable> getTimestableByStudentClass(String studentClass,String term);
    public List<Course> getCourseByMajorName(String majorName);
    public StudentScore getStudentScore(String term,String id);
}
