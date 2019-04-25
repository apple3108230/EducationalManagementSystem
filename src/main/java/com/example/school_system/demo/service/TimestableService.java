package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.Timestable;

import java.util.List;

public interface TimestableService {
    public List<Timestable> getClassTimestable(String classId);
    public List<Timestable> getClassRoomTimestable(String classroomName);
    public List<Timestable> getTimestableByTeacherNameAndTerm(String teacherName,String term);
}
