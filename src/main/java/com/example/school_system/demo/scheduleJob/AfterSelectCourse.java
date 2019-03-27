package com.example.school_system.demo.scheduleJob;

import com.example.school_system.demo.service.CourseSelectionService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class AfterSelectCourse implements Job {

    @Autowired
    private CourseSelectionService courseSelectionService;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
       courseSelectionService.putCourseSelectionToDatabase();
    }
}
