package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.StudentStatusMsg;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentStatusMsgDao {
    public int insertBatchStudentStatusMsg(@Param("studentStatusMsgList") List<StudentStatusMsg> studentStatusMsgList);
}
