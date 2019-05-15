package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Course;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface CourseDao {
    public List<Course> getCourseByCondition(@Param("conditionMap") Map<String,String> conditionMap);
    public int insertCourse(Course course);
    public int updateCourse(Course course);
    public int deleteCourse(String id);
    public String getLastCourseIdByMajorName(String majorName);
    public Course getCourseByMajorId(String majorId);
}
