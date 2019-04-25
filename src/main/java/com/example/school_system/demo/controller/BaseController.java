package com.example.school_system.demo.controller;

import com.example.school_system.demo.exception.UserException;
import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.service.*;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BaseController {

    @Autowired
    protected BaseService baseService;

    @Autowired
    protected MailService mailService;

    @Autowired
    protected DefaultKaptcha defaultKaptcha;

    //生成验证码
    @GetMapping("/kaptchaCode")
    public void kaptchaCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletOutputStream jpgOutStream = response.getOutputStream();
        //获取kaptcha生成的验证码
        String captchaCode = defaultKaptcha.createText();
        HttpSession session = request.getSession();
        //设置缓存有效时间为0
        response.setDateHeader("Expires", 0);
        //设置为禁止缓存防止无法刷新验证码 no-store为绝对禁止缓存
        response.setHeader("Cache-Control", "no-store,no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        //把kaptcha生成的验证码放入session中
        session.setAttribute("captcha_Code", captchaCode);
        //向页面输出图片
        BufferedImage image = defaultKaptcha.createImage(captchaCode);
        ImageIO.write(image, "jpg", jpgOutStream);
        jpgOutStream.flush();
        jpgOutStream.close();
    }

    /**
     * 用于访问无需身份验证的页面
     *
     * @param page
     * @return
     */
    @GetMapping("to/{page}")
    public String toPage(@PathVariable("page") String page) {
        return "/page/" + page;
    }

    /**
     * 用于访问需要身份验证的页面
     *
     * @param page
     * @return
     */
    @GetMapping("to/home/{page}")
    public String toPrivatePage(@PathVariable("page") String page, HttpServletRequest request) throws UserException {
        Subject subject= SecurityUtils.getSubject();
        if (subject == null) {
            throw new UserException("subject is null");
        }
        //不同角色 访问页面的路径也不同
        if (subject.hasRole("student")) {
            return "/page/studentPage/" + page;
        } else if (subject.hasRole("teacher")) {
            return "/page/teacherPage/" + page;
        } else if (subject.hasRole("admin")) {
            return "/page/adminPage/" + page;
        } else {
            throw new UserException("非法用户！");
        }
    }
}
