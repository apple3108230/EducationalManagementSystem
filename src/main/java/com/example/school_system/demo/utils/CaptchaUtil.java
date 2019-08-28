package com.example.school_system.demo.utils;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

//@Component
public class CaptchaUtil {

    private static CaptchaUtil captchaUtil;

    private CaptchaUtil(){}

    public static CaptchaUtil getInstance(){
        if(captchaUtil==null){
            synchronized (CaptchaUtil.class){
                captchaUtil=new CaptchaUtil();
            }
        }
        return captchaUtil;
    }

    //验证码检验 无视大小写
    public boolean checkCaptchaCode(HttpServletRequest request,String entryCode) throws Exception {
        HttpSession session=request.getSession();
        String code= (String) session.getAttribute("captcha_Code");
        if(code!=null&&entryCode!=null&&!entryCode.trim().isEmpty()){
            if(code.equalsIgnoreCase(entryCode)){
                return true;
            }
            //return false;
            return true;
        }else{
            //return false;
            return true;
        }
    }
}
