package com.example.school_system.demo.controller;

import com.example.school_system.demo.scheduleJob.AfterSelectCourse;
import com.example.school_system.demo.utils.TimeUtil;
import com.example.school_system.demo.utils.WebUtil;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private Scheduler scheduler;

    /**
     * 此方法用于设置选课时间后，系统设置默认的选课情况回填时间（在选课结束后2天的凌晨2点执行）
     * @param endTime 开启业务时间
     * @param response HTTPServletResponse
     * @throws SchedulerException
     */
    @RequestMapping("/startScheduleJob")
    @SuppressWarnings("unchecked")
    public void startJob(HttpServletResponse response,String endTime) throws SchedulerException {
        JobDetail jobDetail= JobBuilder.newJob(AfterSelectCourse.class).withIdentity("afterSelectCourse","group1").build();
        Map<String,String> timeMap= TimeUtil.parseTimeString(endTime);
        String day= String.valueOf(Integer.parseInt(timeMap.get("day"))+2);
        String cronString="0 0 2"+" "+day+" "+timeMap.get("month")+" "+"?"+" "+
                timeMap.get("year");
        CronScheduleBuilder cronScheduleBuilder=CronScheduleBuilder.cronSchedule(cronString);
        CronTrigger cronTrigger=TriggerBuilder.newTrigger().withIdentity("afterSelectCourse","group1").withSchedule(cronScheduleBuilder).build();
        scheduler.scheduleJob(jobDetail,cronTrigger);
        scheduler.start();
        WebUtil.printJSON("业务启动成功！",response);
    }

    public void startJob(String endTime) throws SchedulerException {
        JobDetail jobDetail= JobBuilder.newJob(AfterSelectCourse.class).withIdentity("afterSelectCourse","group1").build();
        Map<String,String> timeMap= TimeUtil.parseTimeString(endTime);
        String cronString=timeMap.get("second")+" "+timeMap.get("minute")+" "+timeMap.get("hour")+" "+timeMap.get("day")+" "+timeMap.get("month")+" "+"?"+" "+timeMap.get("year");
        CronScheduleBuilder cronScheduleBuilder=CronScheduleBuilder.cronSchedule(cronString);
        CronTrigger cronTrigger=TriggerBuilder.newTrigger().withIdentity("afterSelectCourse","group1").withSchedule(cronScheduleBuilder).build();
        scheduler.scheduleJob(jobDetail,cronTrigger);
        scheduler.start();
    }
}
