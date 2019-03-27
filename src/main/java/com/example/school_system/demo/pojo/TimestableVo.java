package com.example.school_system.demo.pojo;

import lombok.Data;

/**
 * 用于给前端的课程表填充数据
 */
@Data
public class TimestableVo {
    private Integer id;
    private String className;
    private String courseName;
    private String teacherName;
    private String time;
    private String classroomName;
    private String weeks;
    private String term;
    private String notice;
}
