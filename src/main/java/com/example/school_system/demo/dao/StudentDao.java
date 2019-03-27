package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Course;
import com.example.school_system.demo.pojo.Student;
import com.example.school_system.demo.pojo.StudentStatusMsg;
import com.example.school_system.demo.pojo.Timestable;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface StudentDao {
    public Student getStudentById(String id);
    public void updateInfoById(@Param("map") Map map);
    public StudentStatusMsg getStudentStatusMsgId(String id);
    public List<Timestable> getTimestableByStudentClass(String studentClass);
    public List<Course> getCourseByMajorName(String majorName);
    public Course getCourseByCourseId(String courseId);
}
