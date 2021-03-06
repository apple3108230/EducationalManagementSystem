package com.example.school_system.demo.utils;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CookieUtil {


    private static CookieUtil cookieUtil;

    private CookieUtil(){}

    public static CookieUtil  getInstance(){
        if(cookieUtil==null){
            synchronized (CookieUtil.class){
                cookieUtil=new CookieUtil();
            }
        }
        return cookieUtil;
    }

    //检查cookie是否存在
    public boolean cookieExist(HttpServletRequest request,String cookieName){
        Cookie[] cookies=request.getCookies();
        if(cookies==null){
            return false;
        }
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(cookieName)){
                return true;
            }
        }
        return false;
    }

    //获取cookie值
    public String getCookie(HttpServletRequest request,String cookieName){
        Cookie[] cookies=request.getCookies();
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;
    }

    /**创建一个cookie
     *
     * @param response
     * @param cookieName
     * @param value
     * @param maxAge 单位（s）
     */
    public void creatCookie(HttpServletResponse response, String cookieName, String value,int maxAge){
        Cookie cookie=new Cookie(cookieName,value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
