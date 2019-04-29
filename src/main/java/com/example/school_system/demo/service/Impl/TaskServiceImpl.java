package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.TaskDao;
import com.example.school_system.demo.pojo.ScheduleTask;
import com.example.school_system.demo.service.TaskService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.map.HashedMap;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private TaskDao taskDao;

    final public static int SCHEDULE_TASK_IS_EXIST=0;
    final public static int FAIL_TO_SET_SCHEDULE_TASK_INTO_DATABASE=-1;
    final public static int SET_SCHEDULE_TASK_INTO_DATABASE_IS_SUCCESS=1;
    final public static int FAIL_TO_UPDATE_SCHEDULE_TASK_INTO_DATABASE=-1;
    final public static int SCHEDULE_TASK_DOES_NOT_EXIST=-1;
    final public static int UPDATE_SCHEDULE_TASK_IN_DATABASE_IS_SUCCESS=1;
    final public static int DELETE_SCHEDULE_TASK_IN_DATABASE_IS_SUCCESS=1;
    final public static int FAIL_TO_DELETE_SCHEDULE_TASK_IN_DATABASE=-1;

    @Override
    public int addTask(ScheduleTask scheduleTask) throws SchedulerException, ClassNotFoundException{
        if(!checkTaskExist(scheduleTask.getJobName(),scheduleTask.getJobGroup())){
            JobDetail jobDetail= JobBuilder.newJob((Class<? extends Job>) Class.forName(scheduleTask.getClassName())).withIdentity(scheduleTask.getJobName(),scheduleTask.getJobGroup()).build();
            CronScheduleBuilder cronScheduleBuilder=CronScheduleBuilder.cronSchedule(scheduleTask.getCronExpression());
            CronTrigger cronTrigger=TriggerBuilder.newTrigger().withIdentity(scheduleTask.getJobName(),scheduleTask.getJobGroup()).withSchedule(cronScheduleBuilder).build();
            scheduler.scheduleJob(jobDetail,cronTrigger);
            scheduler.start();
            if(!insertTaskToDataBase(scheduleTask)){
                return FAIL_TO_SET_SCHEDULE_TASK_INTO_DATABASE;
            }
        }else{
            return SCHEDULE_TASK_IS_EXIST;
        }
        return SET_SCHEDULE_TASK_INTO_DATABASE_IS_SUCCESS;
    }

    @Override
    public int editTask(ScheduleTask scheduleTask) throws SchedulerException, SQLException {
        if(checkTaskExist(scheduleTask.getJobName(),scheduleTask.getJobGroup())){
            return SCHEDULE_TASK_DOES_NOT_EXIST;
        }else{
            CronScheduleBuilder cronScheduleBuilder=CronScheduleBuilder.cronSchedule(scheduleTask.getCronExpression());
            CronTrigger cronTrigger=TriggerBuilder.newTrigger().withIdentity(scheduleTask.getJobName(),scheduleTask.getJobGroup()).withSchedule(cronScheduleBuilder).build();
            JobKey jobKey=new JobKey(scheduleTask.getJobName(),scheduleTask.getJobGroup());
            JobDetail jobDetail=scheduler.getJobDetail(jobKey);
            HashSet<Trigger> triggerHashSet=new HashSet<>();
            triggerHashSet.add(cronTrigger);
            scheduler.scheduleJob(jobDetail,triggerHashSet,true);
            if(!editTaskInDataBase(scheduleTask.getTime(),scheduleTask.getCronExpression(),scheduleTask.getScheduleTask())){
                return FAIL_TO_UPDATE_SCHEDULE_TASK_INTO_DATABASE;
            }else{
                return UPDATE_SCHEDULE_TASK_IN_DATABASE_IS_SUCCESS;
            }
        }
    }

    @Override
    public int deleteTask(ScheduleTask scheduleTask) throws SchedulerException, SQLException {
        JobKey jobKey=new JobKey(scheduleTask.getJobName(),scheduleTask.getJobGroup());
        TriggerKey triggerKey=new TriggerKey(scheduleTask.getJobName(),scheduleTask.getJobGroup());
        //停止触发器
        scheduler.pauseTrigger(triggerKey);
        //移除触发器
        scheduler.unscheduleJob(triggerKey);
        //删除任务
        scheduler.deleteJob(jobKey);
        if(!deleteTaskInDataBase(scheduleTask.getScheduleTask())){
            return FAIL_TO_DELETE_SCHEDULE_TASK_IN_DATABASE;
        }else{
            return DELETE_SCHEDULE_TASK_IN_DATABASE_IS_SUCCESS;
        }
    }

    @Override
    public boolean checkTaskExist(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey=new JobKey(jobName,jobGroup);
        return scheduler.checkExists(jobKey);
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public ScheduleTask getScheduleTaskByCondition(Map<String, String> condition) throws SchedulerException {
       ScheduleTask scheduleTask= taskDao.getScheduleTaskByCondition(condition);
       if(scheduleTask!=null){
            if(checkTaskExist(scheduleTask.getJobName(),scheduleTask.getJobGroup())){
                return scheduleTask;
            }else{
                return null;
            }
       }else{
            return null;
       }
    }

    @Override
    public Map<String,Object> getAllScheduleTask() {
        PageHelper.startPage(1,20);
        List<ScheduleTask> scheduleTasks=taskDao.getAllScheduleTask();
        PageInfo<ScheduleTask> info=new PageInfo<>(scheduleTasks);
        Map<String,String> map=new HashedMap<>();
        //由于使用forEach方法遍历list去删除list中的元素会出现java.util.ConcurrentModificationException异常，所以使用索引遍历代替
        for(int i=0;i<scheduleTasks.size();i++){
            ScheduleTask task=scheduleTasks.get(i);
            int total= (int) info.getTotal();
            try {
                if(!checkTaskExist(task.getJobName(),task.getJobGroup())){
                    scheduleTasks.remove(task);
                    total=total-1;
                    int totalPage=total/20;
                    if(total%20!=0){
                        totalPage=totalPage+1;
                        map.put("totalPage",String.valueOf(totalPage));
                    }
                    map.put("totalPage",String.valueOf(totalPage));
                    //这里需要索引值-1保持索引正常
                    i--;
                }
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
        Map<String,Object> scheduleTasksMap=new HashedMap<>();
        scheduleTasksMap.put("totalPage",map);
        scheduleTasksMap.put("scheduleTasks",scheduleTasks);
        return scheduleTasksMap;
    }

    @Transactional(propagation = Propagation.NESTED)
    public boolean insertTaskToDataBase(ScheduleTask scheduleTask){
        int result=taskDao.insertScheduleTask(scheduleTask);
        if(result>=0){
            return true;
        }
        return false;
    }

    private boolean editTaskInDataBase(String time,String cronExpression,String scheduleTask){
        int result=taskDao.updateScheduleTask(time, cronExpression, scheduleTask);
        if(result>=0){
            return true;
        }
        return false;
    }

    public boolean deleteTaskInDataBase(String scheduleTask){
        int result=taskDao.deleteScheduleTask(scheduleTask);
        if(result>=0){
            return true;
        }
        return false;
    }

}
