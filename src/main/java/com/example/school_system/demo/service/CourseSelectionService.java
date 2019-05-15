package com.example.school_system.demo.service;


import com.example.school_system.demo.pojo.SelectCourseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface CourseSelectionService {
    public void insertCourseSelection(String courseId,String studentId,String peopleNum);
    public void setCourseInRedis(Object data) throws IOException;
    public Long selectCourse(String courseId,String studentId) throws IOException;
    public void putCourseSelectionToDatabase() throws IOException;
    public Long cancelSelectedCourse(String courseId,String studentId) throws IOException;
    public List<SelectCourseResult> getSelectCourseResult(String majorName);
    public void uploadTaskForSuperMode(String startTime, String endTime, HttpServletResponse response, HttpServletRequest request) throws IOException;
    public int setScheduleTaskForPreSelectCourse(String time,String scheduleTaskName,String jobName) throws Exception;
    public void uploadTaskForCustomMode(String majorId,String classId,String startTime,String endTime,HttpServletResponse response,HttpServletRequest request) throws Exception;
}
