package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Academy;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademyDao {
    public Academy getAcademyByName(String academyName);
    public List<String> getAllAcademyName();
    public int updateAcademyPeopleNum(Academy academy);
}
