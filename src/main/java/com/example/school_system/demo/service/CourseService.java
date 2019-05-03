package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.Course;

import java.util.List;
import java.util.Map;

public interface CourseService {
    public List<Course> getCourseByCondition(String majorName, String courseName, String classType, String teacherName);
    public boolean insertCourse(Course course);
    public boolean updateCourse(Course course);
    public boolean deleteCourse(String id);
    public Map<String,String> getNewCourseId(Course course);
}
