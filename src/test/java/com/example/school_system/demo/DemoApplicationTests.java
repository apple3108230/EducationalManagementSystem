package com.example.school_system.demo;

import com.example.school_system.demo.pojo.Timestable;
import com.example.school_system.demo.service.StudentService;
import com.example.school_system.demo.utils.WebUtil;
import com.lowagie.text.DocumentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Autowired
    StudentService studentService;

    @Test
    public void contextLoads() throws MessagingException {
    }

    @Test
    public void test(){
        List<Timestable> timestable=studentService.getTimestableByStudentClass("计算机科学与技术一班");
        System.out.println(timestable.get(1).getMajorName());
        System.out.println(timestable.get(2).getMajorName());
    }
}

