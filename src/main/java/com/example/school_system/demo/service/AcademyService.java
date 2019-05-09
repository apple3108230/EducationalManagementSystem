package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.Academy;

import java.util.List;

public interface AcademyService {
    public Academy getAcademyByName(String academyName);
    public List<String> getAllAcademyName();
    public boolean updateAcademyPeopleNum(List<Academy> academyList);
}
