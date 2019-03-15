package com.example.school_system.demo.service;

import javax.mail.*;

public interface MailService {
    public void SendEmail(String randomCode,String to) throws MessagingException;
}
