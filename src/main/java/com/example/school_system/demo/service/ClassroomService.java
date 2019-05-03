package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.Classroom;

import java.util.List;

public interface ClassroomService {
    public List<Classroom> getClassroomByCondition(String classroomName,String classroomType,String academyName);
    public boolean insertClassroom(Classroom classroom);
    public boolean updateClassroom(Classroom classroom);
    public boolean deleteClassroom(String classroomName);
}
