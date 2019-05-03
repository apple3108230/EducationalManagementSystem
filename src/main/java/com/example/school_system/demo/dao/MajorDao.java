package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Major;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MajorDao {
    public List<Major> getAllMajor();
    public List<Major> getMajorByCondition(@Param("conditionMap") Map<String,String> conditionMap);
    public Map<String,String> getLastIdAndMajorIdByAcademy(String academyName);
    public int insertMajor(Major major);
    public int updateMajor(@Param("majorName") String majorName,@Param("id") String id);
    public int deleteMajor(String majorId);

}
