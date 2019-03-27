package com.example.school_system.demo.service;


import com.example.school_system.demo.pojo.SelectCourseResult;

import java.util.List;

public interface CourseSelectionService {
    public void setCourseInRedis(Object data);
    public Long selectCourse(String courseId,String studentId);
    public void putCourseSelectionToDatabase();
    public Long cancelSelectedCourse(String courseId,String studentId);
    public List<SelectCourseResult> getSelectCourseResult(String majorName);
}
