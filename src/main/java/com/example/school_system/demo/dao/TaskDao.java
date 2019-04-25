package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.ScheduleTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskDao {
    public ScheduleTask getScheduleTaskByCondition(@Param("conditionMap") Map<String,String> condition);
    public List<ScheduleTask> getAllScheduleTask();
    public int insertScheduleTask(ScheduleTask scheduleTask);
    public int updateScheduleTask(@Param("time") String time,@Param("scheduleTask") String scheduleTask,@Param("cronExpression") String cronExpression);
    public int deleteScheduleTask(String scheduleTask);
    public ScheduleTask getScheduleTaskByLastId(@Param("conditionMap") Map<String,String> condition);
}
