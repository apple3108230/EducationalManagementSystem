package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.PreSelectCourseTask;
import com.example.school_system.demo.pojo.Role;
import com.example.school_system.demo.pojo.ScheduleTask;
import com.example.school_system.demo.pojo.User;
import java.util.List;
import java.util.Map;

public interface AdminService {
    public boolean updateUserRoleByUsername(Role role);
    public boolean insertBatchPreSelectCourseTask(List<PreSelectCourseTask> preSelectCourseTasks,String startTime,String endTime,String mode);
    public List<PreSelectCourseTask> getTaskByCondition(String academyNameCondition,String majorNameCondition,String classIdCondition);
    public boolean insertTaskForCustomMode(List<PreSelectCourseTask> preSelectCourseTasks,String startTime,String endTime,String mode);
    public boolean deleteTaskByClassName(String className);
    public boolean deleteAllSuperModeTask();
    public User creditAdminAccount();
    public List<PreSelectCourseTask> getAllMajorClassCourse();
    public List<PreSelectCourseTask> getMajorClassCourseByCondition(Map<String,String> conditionMap);
    public boolean insertAcademy(String academyName);
    public boolean updateAcademy(String academyId,String academyName);
}
