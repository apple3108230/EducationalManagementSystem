package com.example.school_system.demo.controller;

import com.example.school_system.demo.exception.UserException;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.utils.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class BaseServiceController extends BaseController{

    /**
     * 重置密码前需要进行验证，验证码通过发送邮件的方式给用户。
     * 若用户没有绑定邮箱，则无法重置密码，只能联系管理员帮用户重置密码
     * 保管验证码的session的有效时间为30分钟，即用户在30分钟内没有使用此验证码，则验证码失效
     * session格式： key:邮箱 value:验证码
     * @param username
     * @param request
     * @throws Exception
     */
    public User sendEmailMethod(String username,HttpServletRequest request) throws Exception {
        if(baseService.getUserByName(username)==null){
            throw new UserException("用户不存在！");
        }else{
            User user= baseService.getUserByName(username);
            if(user.getEmail().isEmpty()){
                throw new UserException("您的账号没有绑定邮箱，无法重置密码!请联系管理员重置");
            }
            String captchaCode=WebUtil.CreatRandomCode();
            try {
                mailService.SendEmail(captchaCode, user.getEmail());
            }catch(MailException exception){
                throw new Exception("无法发送邮件，请联系管理员");
            }
            HttpSession session=request.getSession();
            session.setMaxInactiveInterval(30*60);
            //把需要重置密码的用户名和email存在session中，以便后面对用户名以及对应的验证码进行验证
//            session.setAttribute("username",username);
//            session.setAttribute("email",user.getEmail());
            session.setAttribute(user.getEmail(),captchaCode);
            return user;
        }
    }

    //进入登录界面
    @GetMapping("/home")
    public String index(HttpServletRequest request) {
        //判断是否使用了rememberMe功能，使用了则直接进入系统，否则进入登录页面
        if (CookieUtil.cookieExist(request,"rememberMe")) {
            return toPage("home");
        }
        return toPage("login");
    }

    @RequestMapping("/loginCheck")
    public String loginCheck(HttpServletRequest request) throws Exception {
        String exceptionClassName= (String) request.getAttribute("shiroLoginFailure");
        if(exceptionClassName!=null){
            if(UnknownAccountException.class.getName().equals(exceptionClassName)){
                throw new UserException("用户不存在！");
            }else if(IncorrectCredentialsException.class.getName().equals(exceptionClassName)){
                throw new UserException("密码错误！");
            }else if("captchaCodeError".equals(exceptionClassName)){
                throw new UserException("验证码错误！");
            }else{
                throw new Exception();
            }
        }
        return toPage("home");
    }

//    @GetMapping("/toForgetPassword")
//    public String toForgetPassword(){
//        return "forget_password";
//    }

    /**
     * 将username和email保存到HttpSession中，以便后面需要重新发送邮件时使用
     * @param username
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/sendEmail")
    public void sendEmail(String username, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user=sendEmailMethod(username,request);
        HttpSession session=request.getSession();
        session.setAttribute("username",username);
        session.setAttribute("email",user.getEmail());
        WebUtil.printJSON("发送成功！",response);
    }

    /**
     * 此方法用于用户未接收到邮件然后重新发送邮件
     * @param request
     * @throws Exception
     */
    @GetMapping("/sendEmailAgain")
    public void sendEmailAgain(HttpServletRequest request,HttpServletResponse response) throws Exception {
        HttpSession session=request.getSession();
        String username= (String) session.getAttribute("username");
        User user=sendEmailMethod(username,request);
        WebUtil.printJSON("发送成功！",response);
    }

    @GetMapping("/checkCaptchaBeforeResetPwd")
    private void checkCaptchaBeforeResetPwd(HttpServletRequest request,HttpServletResponse response,String captchaCode){
        HttpSession session=request.getSession();
        String code= (String) session.getAttribute((String) session.getAttribute("email"));
        if(captchaCode.equals(code)){
            WebUtil.printJSON("success",response);
        }else{
            WebUtil.printJSON("验证码错误！",response);
        }
    }

//    @GetMapping("/toPreResetPwd")
//    public String toPreResetPwd(){
//        return "pre_resetPwd";
//    }
//
//    @GetMapping("/toResetPwd")
//    public String toResetPwd(){
//        return "reset_pwd";
//    }


    /**
     * 重置密码 用户名作为salt，加密次数为1024次，加密方式为MD5,数据库保存salt值
     * @param password
     * @param request
     * @param response
     * @throws UserException
     */
    @PostMapping("/resetPwd")
    public void resetPwd(String password,HttpServletRequest request,HttpServletResponse response) throws UserException {
        User user= (User) request.getSession().getAttribute("user");
        String username=user.getUsername();
        if(username.isEmpty()){
            throw new UserException("无法重置密码！（原因：无法找到用户）");
        }
        else{
            //用户名作为salt
            ByteSource source=(ByteSource.Util.bytes(username));
            //加密次数为1024次
            int hashInterations=1024;
            String newPwd=(new SimpleHash("MD5",password,source,hashInterations)).toString();
            String salt=source.toString();
            baseService.resetPwdByName(newPwd,salt,username);
            WebUtil.printJSON("重置成功！",response);
        }
    }


//    @GetMapping("/logout")
//    public void logout(ServletRequest request,HttpServletResponse response){
//        //Subject subject= SecurityUtils.getSubject();
//        ServletContext context=request.getServletContext();
//        HttpServletRequest req=(HttpServletRequest) request;
//        HttpSession session=req.getSession();
//        try{
//            context.removeAttribute("user");
//            session.invalidate();
//            //subject.logout();
//        }catch(SessionException e){
//            WebUtil.printJSON("无法退出系统！原因："+e.getMessage(),response);
//        }
//        WebUtil.printJSON("退出成功！",response);
//    }


}
