package com.example.school_system.demo.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Student implements Serializable {
    private int id;
    private String name;
    private String sex;
    private String age;
    private String birthday;
    private String addr;
    private String tel;
    private String academy;
    private String studentClass;
}
