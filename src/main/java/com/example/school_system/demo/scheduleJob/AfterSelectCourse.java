package com.example.school_system.demo.scheduleJob;

import com.example.school_system.demo.service.CourseSelectionService;
import com.example.school_system.demo.utils.RedisUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class AfterSelectCourse implements Job {

    @Autowired
    private CourseSelectionService courseSelectionService;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            courseSelectionService.putCourseSelectionToDatabase();
            if(RedisUtil.process!=null){
                //若是系统自动启动的redis服务，则在此业务结束后将自动关闭
                RedisUtil.process.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
