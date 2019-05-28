package com.example.school_system.demo.controller;

import com.example.school_system.demo.exception.OtherException;
import com.example.school_system.demo.exception.UserException;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.utils.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.json.simple.JSONObject;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Time;
import java.text.ParseException;

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
        if(baseService.getUserByUserName(username)==null){
            throw new UserException("用户不存在！");
        }else{
            User user= baseService.getUserByUserName(username);
            if(user.getEmail().isEmpty()){
                throw new UserException("您的账号没有绑定邮箱，无法重置密码!请绑定邮箱后再重置密码！");
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
    public String index(HttpServletRequest request) throws UserException {
        //判断是否使用了rememberMe功能，使用了则直接进入系统，否则进入登录页面
        if (CookieUtil.cookieExist(request,"rememberMe")) {
            return toPrivatePage("welcomePage",request);
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
            }else if("UnknownAccountRoleException".equals(exceptionClassName)){
                throw new UserException("非法账号！");
            } else{
                throw new Exception();
            }
        }
        return toPage("login");
    }

    @GetMapping("/bindingEmail")
    public void bindingEmail(String username1,String email,HttpServletResponse response,HttpServletRequest request){
        JSONObject json=new JSONObject();
        if(username1==null||username1.isEmpty()){
            User user= (User) request.getSession().getAttribute("user");
            if(user==null){
                json.put("message","username is null");
            }
            username1=user.getUsername();
        }
        int effect=baseService.insertEmailByUserName(username1, email);
        if(effect>=0){
            json.put("message","绑定成功！");
        }else{
            json.put("message","绑定失败，请联系管理员！");
        }
        WebUtil.printJSON(json.toJSONString(),response);
    }

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
        JSONObject json=new JSONObject();
        json.put("message","发送成功！");
        WebUtil.printJSON(json.toJSONString(),response);
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
        JSONObject json=new JSONObject();
        json.put("message","发送成功！");
        WebUtil.printJSON(json.toJSONString(),response);
    }

    @GetMapping("/checkCaptchaBeforeResetPwd")
    public void checkCaptchaBeforeResetPwd(HttpServletRequest request,HttpServletResponse response,String captchaCode){
        HttpSession session=request.getSession();
        String code= (String) session.getAttribute((String) session.getAttribute("email"));
        JSONObject json=new JSONObject();
        if(captchaCode.equals(code)){
            json.put("message","success");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","验证码错误！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    /**
     * 重置密码 用户名作为salt，加密次数为1024次，加密方式为MD5,数据库保存salt值
     * 此方法适用于在系统内重置密码或在系统外重置密码
     * 若登录时检测到账号的密码为初始密码，则获取存放在HTTPSession中的username进行重置密码
     * @param password
     * @param request
     * @param response
     * @throws UserException
     */
    @PostMapping("/resetPwd")
    public void resetPwd(String password,HttpServletRequest request,HttpServletResponse response) throws UserException {
        String username=null;
        User user= (User) request.getSession().getAttribute("user");
        String usernameForSession=(String) request.getSession().getAttribute("username");
        //若无法获取HTTPSession中的user，则尝试从HTTPSession中获取username
        if(user!=null){
            username=user.getUsername();
        }else{
            if(usernameForSession!=null){
                username= usernameForSession;
            }else{
                throw new UserException("unknown login account!");
            }
        }
        //用户名作为salt
        ByteSource source=(ByteSource.Util.bytes(username));
        //加密次数为1024次
        int hashInterations=1024;
        String newPwd=(new SimpleHash("MD5",password,source,hashInterations)).toString();
        String salt=source.toString();
        int effect= baseService.resetPwdByUserName(newPwd,salt,username);
        JSONObject json=new JSONObject();
        if(effect>=0){
            json.put("message","重置成功！");
            SecurityUtils.getSubject().logout();
            WebUtil.printJSON(json.toJSONString(),response);
        } else{
            json.put("message","重置失败，请联系管理员！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @GetMapping("/checkTime")
    public void checkTime(@RequestParam("startTime")String startTime,@RequestParam("endTime")String endTime,HttpServletResponse response) throws ParseException {
        int result=TimeUtil.checkTime(startTime, endTime);
        JSONObject json=new JSONObject();
        if(result==TimeUtil.ENDTIME_LESS_THAN_NOWTIME){
            json.put("message","您输入的结束时间小于当前时间！请重新输入！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
        if(result==TimeUtil.STARTTIME_LESS_THAN_NOWTIME){
            json.put("message","您输入的开始时间小于当前时间！请重新输入！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
        if(result==TimeUtil.ENDTIME_LESS_THAN_STARTTIME){
            json.put("message","您输入的结束时间小于开始时间！请重新输入！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
        if(result== TimeUtil.TIME_IS_ILLEGAL){
            json.put("message","您输入的时间不合法！请重新输入！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
        if(result==TimeUtil.OK){
            json.put("message","success");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

}
