package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.MajorClass;

import java.util.List;

public interface MajorClassService {
    public List<MajorClass> getMajorClassByCondition(String majorName,String className);
    public boolean insertMajorClass(String majorName,int majorClassNum);
    public boolean deleteMajorClass(String className);
}
