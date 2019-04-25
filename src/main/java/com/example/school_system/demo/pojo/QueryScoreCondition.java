package com.example.school_system.demo.pojo;

import lombok.Data;

@Data
public class QueryScoreCondition {

    private String termCondition;
    private String studentIdCondition;
    private String courseCondition;
    private String classIdCondition;
    private String otherCondition;
    private String pageNum;

}
