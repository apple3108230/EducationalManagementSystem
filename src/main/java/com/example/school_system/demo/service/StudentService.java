package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.Course;
import com.example.school_system.demo.pojo.Student;
import com.example.school_system.demo.pojo.StudentStatusMsg;
import com.example.school_system.demo.pojo.Timestable;
import java.util.List;
import java.util.Map;

public interface StudentService {
    public Student getStudentById(String id);
    public void updateInfoById(Map map);
    public StudentStatusMsg getStudentStatusMsgId(String id);
    public List<Timestable> getTimestableByStudentClass(String studentClass);
    public List<Course> getCourseByMajorName(String majorName);
}
