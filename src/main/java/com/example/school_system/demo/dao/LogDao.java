package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.SensitiveOperation;
import com.example.school_system.demo.pojo.SystemError;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogDao {
    public List<SensitiveOperation> getSensitiveOperationLog();
    public List<SystemError> getSystemError();
    public int insertSensitiveOperationLog(SensitiveOperation sensitiveOperation);
    public int insertSystemErrorLog(SystemError systemError);
}
