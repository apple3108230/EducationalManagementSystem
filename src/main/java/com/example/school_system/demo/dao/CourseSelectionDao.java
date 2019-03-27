package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Course;
import com.example.school_system.demo.pojo.SelectCourseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSelectionDao {
    public void insertCourseSelection(@Param("courseId") String courseId,@Param("studentId") String studentId,@Param("peopleNum") String peopleNum);
    public List<Course> defaultGetCourse();
    public List<SelectCourseResult> getSelectCourseResult(String majorName);
}
