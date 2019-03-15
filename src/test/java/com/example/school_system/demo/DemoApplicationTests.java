package com.example.school_system.demo;

import com.example.school_system.demo.utils.WebUtil;
import com.lowagie.text.DocumentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.mail.MessagingException;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DemoApplicationTests {

//
//    @Autowired
//    StudentService studentService;

    @Test
    public void contextLoads() throws MessagingException {
    }

    @Test
    public void test() throws IOException, DocumentException {
        String htmlUrl="C:\\Users\\Administrator\\Desktop\\schoolSys\\src\\main\\resources\\templates\\page\\studentPage\\student_status_msg.html";
        String pdfUrl="C:\\Users\\Administrator\\Desktop\\schoolSys\\src\\main\\resources\\templates\\pdf\\student-status-msg\\img\\"+"01234567"+".pdf";
        //WebUtil.createPDF(htmlUrl,pdfUrl);
    }
}

