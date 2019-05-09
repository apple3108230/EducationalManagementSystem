package com.example.school_system.demo.service;

import com.example.school_system.demo.pojo.SensitiveOperation;
import com.example.school_system.demo.pojo.SystemError;

public interface LogService {
    public void insertSensitiveOperationLog(SensitiveOperation sensitiveOperation);
    public void insertSystemErrorLog(SystemError systemError);
}
