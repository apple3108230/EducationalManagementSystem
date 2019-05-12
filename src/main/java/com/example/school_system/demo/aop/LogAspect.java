package com.example.school_system.demo.aop;

import com.example.school_system.demo.exception.OtherException;
import com.example.school_system.demo.exception.ResolveExcelException;
import com.example.school_system.demo.exception.SystemException;
import com.example.school_system.demo.exception.UserException;
import com.example.school_system.demo.pojo.SystemError;
import com.example.school_system.demo.service.LogService;
import com.example.school_system.demo.utils.TimeUtil;
import org.apache.shiro.session.UnknownSessionException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.EOFException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * 此处用于收集系统异常日志 aop
 */
@Configuration
@Aspect
public class LogAspect {

    @Autowired
    private LogService logService;

//    @Pointcut("execution(* com.example.school_system.demo.*.*.*(..))")
//    private void pointCut(){}
//
//    @AfterThrowing(pointcut = "pointCut()",throwing = "throwable")
    public void getAllSystemErrorToLog(JoinPoint joinPoint,Throwable throwable){
        if(!(throwable instanceof UnknownSessionException)|| !(throwable instanceof EOFException)||!(throwable instanceof OtherException)||!(throwable instanceof ResolveExcelException)||!(throwable instanceof UserException)||!(throwable instanceof SystemException)||!(throwable instanceof SQLException)){
            System.out.println("resolve system error....");
            //类名
            String className=joinPoint.getTarget().getClass().getName();
            //方法名
            String methodName=joinPoint.getSignature().getName();
            //异常信息
            String message=throwable.getMessage();
            SystemError systemError=new SystemError();
            systemError.setClassName(className);
            systemError.setMessage(message);
            systemError.setMethodName(methodName);
            systemError.setTime(TimeUtil.getNowTime());
            logService.insertSystemErrorLog(systemError);
            System.out.println("finish！");
        }
    }

}
