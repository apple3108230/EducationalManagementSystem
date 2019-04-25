package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Timestable;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimestableDao {
    public List<Timestable> getClassTimestable(String classId);
    public List<String> getAllClassId();
    public List<Timestable> getClassRoomTimestable(String classroomName);
    public List<String> getAllClassRoomName();
    public List<Timestable> getTimestableByTeacherNameAndTerm(@Param("teacherName") String teacherName,@Param("term") String term);
}
