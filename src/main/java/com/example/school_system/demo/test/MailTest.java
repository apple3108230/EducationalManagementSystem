package com.example.school_system.demo.test;

import com.example.school_system.demo.utils.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Random;

public class MailTest {

    @Autowired
    JavaMailSender javaMailSender;

    private String text="欢迎使用JavaMail,您的验证码是：";
    public void sendMail(String randomCode,String to){
        String subject="826628217@qq.com";
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom(subject);
        mailMessage.setTo(to);
        mailMessage.setText(text+randomCode);
        javaMailSender.send(mailMessage);
    }

    public static void main(String[] args){
        MailTest mailTest=new MailTest();
        mailTest.sendMail(WebUtil.CreatRandomCode(),"liushijun_877@126.com");
    }
}
