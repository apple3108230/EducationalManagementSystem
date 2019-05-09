package com.example.school_system.demo.service;


import com.example.school_system.demo.pojo.SelectCourseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface CourseSelectionService {
    public void insertCourseSelection(String courseId,String studentId,String peopleNum);
    public void setCourseInRedis(Object data);
    public Long selectCourse(String courseId,String studentId);
    public void putCourseSelectionToDatabase();
    public Long cancelSelectedCourse(String courseId,String studentId);
    public List<SelectCourseResult> getSelectCourseResult(String majorName);
    public void uploadTaskForSuperMode(String startTime, String endTime, HttpServletResponse response, HttpServletRequest request);
    public int setScheduleTaskForPreSelectCourse(String time,String scheduleTaskName,String jobName) throws Exception;
    public void uploadTaskForCustomMode(String majorId,String classId,String startTime,String endTime,HttpServletResponse response,HttpServletRequest request) throws Exception;
}
