package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.MajorClass;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MajorClassDao {
    public List<MajorClass> getMajorClassByCondition(@Param("conditionMap") Map<String,String> conditionMap);
    public int insertMajorClass(@Param("majorClassList") List<MajorClass> majorClassList);
    public int deleteMajorClass(String className);
}
