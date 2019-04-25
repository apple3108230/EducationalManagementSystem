package com.example.school_system.demo.pojo;

import com.example.school_system.demo.utils.StringUtil;
import lombok.Data;

@Data
public class Course {

    private String id;
    private String courseName;
    private String teacherName;
    private String classHour;
    private String teachClassHour;
    private String computerClassHour;
    private String classType;
    private String majorName;
    private Major major;
    private String credit;
    private String majorClass;

    public CourseVo toCourseVo(){
        CourseVo courseVo=new CourseVo();
        courseVo.setId(getId());
        courseVo.setClassHour(getClassHour());
        courseVo.setClassType(getClassType());
        courseVo.setComputerClassHour(getComputerClassHour());
        courseVo.setCourseName(getCourseName());
        courseVo.setMajorName(getMajorName());
        courseVo.setTeachClassHour(getTeachClassHour());
        courseVo.setPeopleNum(getMajor().getPeopleNum());
        courseVo.setTeacherName(getTeacherName());
        return courseVo;
    }

}