package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.SensitiveOperation;
import com.example.school_system.demo.pojo.SystemError;
import org.springframework.stereotype.Repository;

@Repository
public interface LogDao {
    public int insertSensitiveOperationLog(SensitiveOperation sensitiveOperation);
    public int insertSystemErrorLog(SystemError systemError);
}
