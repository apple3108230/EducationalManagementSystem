package com.example.school_system.demo.pojo;

import lombok.Data;

@Data
public class ScheduleJob {
    private String className;
    private String jobName;
    private String jobGroup;
    private String cronExpression;
}
