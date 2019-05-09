package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.pojo.Academy;
import com.example.school_system.demo.service.AcademyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AcademyServiceImpl implements AcademyService {

    @Autowired
    private AcademyService academyService;

    @Override
    public Academy getAcademyByName(String academyName) {
        return academyService.getAcademyByName(academyName);
    }
}
