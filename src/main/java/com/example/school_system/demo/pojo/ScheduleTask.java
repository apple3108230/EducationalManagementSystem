package com.example.school_system.demo.pojo;

import lombok.Data;

@Data
public class ScheduleTask extends ScheduleJob {
    private Integer id;
    private String scheduleTask;
    private String time;
}