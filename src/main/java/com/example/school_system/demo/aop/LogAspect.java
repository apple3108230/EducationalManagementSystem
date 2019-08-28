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
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.EOFException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
     * 无法切入工具类，因为无法切入静态方法（需要使用静态切点）
     */
    @Pointcut("execution(* com.example.school_system.demo.*.*.*(..))&& !execution(* com.example.school_system.demo.aop.LogAspect.*(..))")
    private void pointCut(){}

    @AfterThrowing(pointcut = "pointCut()",throwing = "throwable")
    public void getAllSystemErrorToLog(JoinPoint joinPoint,Throwable throwable) throws NoSuchMethodException {
        //不记录UnknownSessionException、EOFException、exception包下的异常类型和AuthenticationException
        if(!(throwable instanceof UnknownSessionException)|| !(throwable instanceof EOFException)||!(throwable instanceof SQLException)){
            System.out.println("resolve system error....");
            //类名
            String className=joinPoint.getTarget().getClass().getName();
            //方法名
            String methodName=joinPoint.getSignature().getName();
//            Method method=((MethodSignature)joinPoint.getSignature()).getMethod();
//            Annotation annotation=method.getAnnotation(GetMapping.class);
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
