package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    JavaMailSender javaMailSender;

    /**
     * 以邮件的方式，将验证码发送到用户邮箱（发送的有点满平均19s）
     * @param randomCode
     * @param to
     * @throws MessagingException
     */
    @Override
    public void SendEmail(String randomCode, String to) throws MessagingException {
        String from="826628217@qq.com";
        SimpleMailMessage mailMessage=new SimpleMailMessage();

        mailMessage.setFrom(from);
        mailMessage.setTo(to);
        mailMessage.setSubject("重置密码");
        mailMessage.setText("欢迎使用本教务系统。以下是您重置密码时所需要的验证码："+randomCode+"。\n"+
                            "请在30分钟内使用此验证码，否则失效。");
        javaMailSender.send(mailMessage);
    }
}
