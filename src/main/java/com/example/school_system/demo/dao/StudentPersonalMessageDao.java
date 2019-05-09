package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Student;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentPersonalMessageDao {
    public int insertBatchStudentPersonalMessage(@Param("list") List<Student> studentList);
    public String getLastStudentIdByClassId(String classId);
}
