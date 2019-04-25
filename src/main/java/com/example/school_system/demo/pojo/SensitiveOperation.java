package com.example.school_system.demo.pojo;

import lombok.Data;

@Data
public class SensitiveOperation {
    private Integer id;
    private String action;
    private String time;
    private String operator;
}