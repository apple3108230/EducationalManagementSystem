package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.ScheduleTask;
import org.apache.ibatis.annotations.Param;
import org.quartz.SchedulerException;
import java.sql.SQLException;
import java.util.Map;

public interface TaskService {
    public int addTask(ScheduleTask scheduleTask) throws SchedulerException, ClassNotFoundException, SQLException;
    public int editTask(ScheduleTask scheduleTask) throws SchedulerException, SQLException;
    public int deleteTask(ScheduleTask scheduleTask) throws SchedulerException, SQLException;
    public boolean checkTaskExist(String jobName,String jobGroup) throws SchedulerException;
    public ScheduleTask getScheduleTaskByCondition(@Param("conditionMap") Map<String,String> condition) throws SchedulerException;
    public Map<String,Object> getAllScheduleTask();
}
