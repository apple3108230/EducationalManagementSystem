package com.example.school_system.demo.aop;

import com.example.school_system.demo.utils.RedisUtil;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Aspect
@Configuration
public class RedisAspect {

    @Pointcut("execution(* com.example.school_system.demo.service.Impl.CourseSelectionServiceImpl.*(..)) && withoutCut() && withoutCut1() && withoutCut2()")
    private void pointCut(){}
    @Pointcut("execution(* com.example.school_system.demo.Interceptor.PreSelectCourseResultInterceptor.postHandle(..))")
    private void extraCut(){}
    @Pointcut("!execution(* com.example.school_system.demo.service.Impl.CourseSelectionServiceImpl.setScheduleTaskForPreSelectCourse(..))")
    private void withoutCut(){}
    @Pointcut("!execution(* com.example.school_system.demo.service.Impl.CourseSelectionServiceImpl.getSelectCourseResult(..))")
    private void withoutCut1(){}
    @Pointcut("!execution(* com.example.school_system.demo.service.Impl.CourseSelectionServiceImpl.insertCourseSelection(..))")
    private void withoutCut2(){}

    @Before("pointCut()||extraCut()")
    public void before() throws IOException, InterruptedException {
        RedisUtil redisUtil=new RedisUtil();
        if(!redisUtil.redisConnectionIsExist()){
            redisUtil.autoOpenRedisServer();
            Thread.sleep(5000);
        }
    }

}
