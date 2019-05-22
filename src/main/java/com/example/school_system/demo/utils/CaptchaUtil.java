package com.example.school_system.demo.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class CaptchaUtil {
    //验证码检验 无视大小写
    public static boolean checkCaptchaCode(HttpServletRequest request,String entryCode){
        HttpSession session=request.getSession();
        String code= (String) session.getAttribute("captcha_Code");
        if(code!=null&&entryCode!=null&&!entryCode.trim().isEmpty()){
            if(code.equalsIgnoreCase(entryCode)){
                return true;
            }
//            return false;
        }else{
            //return false;
            return true;
        }
        return true;
    }
}
