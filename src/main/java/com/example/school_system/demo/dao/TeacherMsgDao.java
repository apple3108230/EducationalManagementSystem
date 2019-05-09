package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.TeacherMsg;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeacherMsgDao {
    public int insertBatchTeacherMsg(@Param("teacherMsgList") List<TeacherMsg> teacherMsgList);
    public String getLastTeacherId(String academyId);
}
