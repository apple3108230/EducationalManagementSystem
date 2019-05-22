package com.example.school_system.demo.aop;

import com.example.school_system.demo.exception.OtherException;
import com.example.school_system.demo.exception.ResolveExcelException;
import com.example.school_system.demo.exception.SystemException;
import com.example.school_system.demo.exception.UserException;
import com.example.school_system.demo.pojo.SystemError;
import com.example.school_system.demo.service.LogService;
import com.example.school_system.demo.utils.TimeUtil;
import org.apache.shiro.authc.AuthenticationException;
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

    /**
     * 这里切点不能切到LogAspect类，否则当数据库连接异常时，记录系统异常日志功能会进入死循环
     */
    @Pointcut("execution(* com.example.school_system.demo.*.*.*(..))&& !execution(* com.example.school_system.demo.aop.LogAspect.*(..))")
    private void pointCut(){}

    @AfterThrowing(pointcut = "pointCut()",throwing = "throwable")
    public void getAllSystemErrorToLog(JoinPoint joinPoint,Throwable throwable){
        String packageName=throwable.getClass().getPackage().getName();
        //不记录UnknownSessionException、EOFException、exception包下的异常类型和AuthenticationException
        if(!(throwable instanceof UnknownSessionException)|| !(throwable instanceof EOFException)||!(throwable instanceof SQLException)){
            System.out.println("resolve system error....");
            //类名
            String className=joinPoint.getTarget().getClass().getName();
            //方法名
            String methodName=joinPoint.getSignature().getName();
            //异常信息
            String message="";
            StackTraceElement[] stackTraceElements=throwable.getStackTrace();
            for(StackTraceElement stackTraceElement:stackTraceElements){
                message += "\tat " + stackTraceElement + "\r\n";
            }
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
