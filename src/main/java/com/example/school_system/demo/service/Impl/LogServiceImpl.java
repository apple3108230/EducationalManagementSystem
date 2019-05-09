package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.LogDao;
import com.example.school_system.demo.pojo.SensitiveOperation;
import com.example.school_system.demo.pojo.SystemError;
import com.example.school_system.demo.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogDao logDao;


    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void insertSensitiveOperationLog(SensitiveOperation sensitiveOperation) {
        logDao.insertSensitiveOperationLog(sensitiveOperation);
    }

    @Override
    public void insertSystemErrorLog(SystemError systemError) {
        logDao.insertSystemErrorLog(systemError);
    }
}
