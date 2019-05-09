package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.Major;
import java.util.List;
import java.util.Map;

public interface MajorService {
    public List<Major> getAllMajor();
    public List<Major> getMajorByCondition(Map<String,String> conditionMap);
    public Map<String,String> getLastIdAndMajorIdByAcademy(String academyName);
    public boolean insertMajor(Major major,String xuezhi);
    public boolean updateMajor(String majorName,String id);
    public boolean deleteMajor(String majorId);
    public Map<String,String> getNewIdAndMajorId(Map<String,String> info);
    public boolean updateMajorPeopleNum(List<Major> majors);
}
