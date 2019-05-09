package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Academy;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademyDao {
    public Academy getAcademyByName(String academyName);
}
