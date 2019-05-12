package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.SensitiveOperation;
import com.example.school_system.demo.pojo.SystemError;

import java.util.List;

public interface LogService {
    public List<SensitiveOperation> getSensitiveOperationLog();
    public List<SystemError> getSystemError();
    public void insertSensitiveOperationLog(SensitiveOperation sensitiveOperation);
    public void insertSystemErrorLog(SystemError systemError);
}
