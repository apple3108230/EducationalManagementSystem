package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.PreSelectCourseTask;
import com.example.school_system.demo.pojo.Role;
import com.example.school_system.demo.pojo.ScheduleTask;
import com.example.school_system.demo.pojo.User;
import java.util.List;

public interface AdminService {
    public boolean updateUserRoleByUsername(Role role);
    public boolean insertBatchPreSelectCourseTask(List<PreSelectCourseTask> preSelectCourseTasks,String startTime,String endTime);
    public List<PreSelectCourseTask> getTaskByCondition(String academyNameCondition,String majorNameCondition,String classIdCondition);
    public boolean insertTaskForCustomMode(List<PreSelectCourseTask> preSelectCourseTasks,String startTime,String endTime);
    public boolean deleteTaskByClassName(String className);
    public User creditAdminAccount();
}
