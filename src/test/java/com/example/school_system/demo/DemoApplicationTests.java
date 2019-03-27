package com.example.school_system.demo;


import com.example.school_system.demo.controller.AdminController;
import com.example.school_system.demo.service.CourseSelectionService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Autowired
    private CourseSelectionService courseSelectionService;
    @Autowired
    private AdminController adminController;


    @Test
    public void contextLoads() throws MessagingException {
    }

    @Test
    public void test() throws SchedulerException, InterruptedException, ParseException {
//        String endTime="2019-3-27 10:05:00";
//        Map<String,String> timeMap=new HashMap<>();
//        timeMap=TimeUtil.parseTimeString(endTime);
//        JobDetail detail= JobBuilder.newJob(JobTest.class).withIdentity("test1","testGroup").build();
//        CronScheduleBuilder cronScheduleBuilder=CronScheduleBuilder.cronSchedule("30 27 15 25 3 ? 2019");
//        CronTrigger trigger=TriggerBuilder.newTrigger().withIdentity("test1","testGroup").withSchedule(cronScheduleBuilder).build();
//        scheduler.scheduleJob(detail,trigger);
//        scheduler.start();
//        Thread.sleep(100000);
//        String studentId="012345678";
//        String courseId="081721401";
//        System.out.println(courseSelectionService.selectCourse(courseId,studentId));
//        adminController.startJob(endTime);
//        Thread.sleep(100000);
        JSONParser parser=new JSONParser();
        JSONObject json= (JSONObject) parser.parse("{\"student0\":\"012345678\"}");
        System.out.println(json.get("student0"));
    }
}

