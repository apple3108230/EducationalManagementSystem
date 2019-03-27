package com.example.school_system.demo.pojo;

import lombok.Data;

@Data
public class CourseVo {
    private String id;
    private String courseName;
    private String teacherName;
    private String classHour;
    private String teachClassHour;
    private String computerClassHour;
    private String classType;
    private String majorName;
    private String peopleNum;
}
