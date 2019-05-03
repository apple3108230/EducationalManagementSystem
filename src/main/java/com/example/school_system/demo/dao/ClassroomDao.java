package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.Classroom;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.List;

@Repository
public interface ClassroomDao {
    public List<Classroom> getClassroomByCondition(@Param("conditionMap") Map<String,String> conditionMap);
    public int insertClassroom(Classroom classroom);
    public int updateClassroom(Classroom classroom);
    public int deleteClassroom(String classroomName);
}
