package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.DeleteScoreInfo;
import com.example.school_system.demo.pojo.StudentScore;
import com.example.school_system.demo.pojo.TeacherMsg;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface TeacherDao {
    public TeacherMsg getTeacherMsgById(String id);

}
