package com.example.school_system.demo.shiro;

import com.example.school_system.demo.exception.UnknownAccountRoleException;
import com.example.school_system.demo.exception.UserException;
import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.service.BaseService;
import com.example.school_system.demo.utils.CaptchaUtil;
import com.example.school_system.demo.utils.WebUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



/**
 * 自定义一个表单认证过滤器 实现自己控制表单认证流程（自动拦截表单中的username和password）
 */
public class CustomFormAuthenticationFilter extends FormAuthenticationFilter {

    @Value("${student.account.original-password}")
    private String defaultStudentPassword;
    @Value("${teacher.account.original-password}")
    private String defaultTeacherPassword;
    @Value("${admin.account.original-password}")
    private String defaultAdminPassword;

    @Autowired
    private BaseService baseService;


//    @Override
//    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
//        Subject subject=getSubject(request, response);
//        if(!subject.isAuthenticated()&&subject.isRemembered()){
//            Session session=subject.getSession();
//            if(session.getAttribute("user")==null){
//                User user= (User) subject.getPrincipal();
//                session.setAttribute("user",user);
//            }
//        }
//        return subject.isAuthenticated()||subject.isRemembered();
//    }

    /**
     *
     * @param request
     * @param response
     * @return 是否为合法用户
     * @throws Exception
     * 若用户没有进行认证，但是使用了“记住我”功能，则可以自动登录(注意：在使用了shiro的情况下，通过实现filter来实现自动登录是行不通的，所以交给表单验证处理)
     * 重写表单验逻辑，验证表单前先进行验证验证码是否正确，正确才进行验凭证
     * isLoginSubmission()是用于判断是否为post请求
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        //若用户没有进行认证，但是使用了“记住我”功能，则可以自动登录
        if (autoLogin(httpServletRequest)) {
            return true;
        }
        if (isLoginRequest(request, response)) {
            //验证用户是否合法和验证码是否正确
            if (isLoginSubmission(request, response)) {
                //判断验证码是否正确
                if (CaptchaUtil.getInstance().checkCaptchaCode(httpServletRequest, httpServletRequest.getParameter("captchaCode"))) {
                    String username=request.getParameter("username");
                    String password=request.getParameter("password");
                    //System.out.println("username:"+username+" password:"+password);
                    String MD5Password=new SimpleHash("MD5",password,ByteSource.Util.bytes(username),1024).toString();
                    User user=new User(username,MD5Password);
                    boolean isDefaultPassword = false;
                    try {
                        /**
                         * 判断此用户的密码是否为初始密码。若是初始密码，则需要重置密码后才能登录；否则进入executeLogin方法，判断账号和密码是否正确
                         * 此方法会在判断输入的账号和密码进行判断。若账号或密码不存在，则抛出IncorrectCredentialsException；
                         *                                       若账号不存在，则抛出UnknownAccountException；
                         *                                       若账号的角色不合法，则抛出UnknownAccountRoleException；
                         *                                       抛出异常后，进入onLoginFailure方法
                         */
                        isDefaultPassword=checkAccountPasswordIsDefault(user);
                    }catch (AuthenticationException e){
                        //生成用户填写的登录信息的UsernamePasswordToken
                        UsernamePasswordToken token=new UsernamePasswordToken(username,password.toCharArray());
                        onLoginFailure(token,e,request,response);
                    }
                    //若用户的密码不是初始密码，则进入executeLogin方法，判断账号和密码是否正确；若是初始密码，则需要重置密码后才能登录
                    if(!isDefaultPassword){
                        executeLogin(request,response);
                    }else{
                        JSONObject json=new JSONObject();
                        json.put("message","您需要修改账号密码才能进入系统！");
                        httpServletRequest.getSession().setAttribute("username",username);
                        WebUtil.printJSON(json.toJSONString(), httpServletResponse);
                        return false;
                    }
                    //System.out.println("是否为初始密码："+isDefaultPassword);
                } else {
                    if (WebUtil.isAjax(httpServletRequest)) {
                        JSONObject json=new JSONObject();
                        json.put("message","验证码错误");
                        WebUtil.printJSON(json.toJSONString(), httpServletResponse);
                        return false;
                    } else {
                        httpServletRequest.setAttribute("shiroLoginFailure", "captchaCodeError");
                        return true;
                    }
                }
            } else {
                return true;
            }
        }else{
            if(WebUtil.isAjax(httpServletRequest)){
                return false;
            }else{
                //返回/loginCheck,重定向到登录页面，重新提交给此方法
                saveRequestAndRedirectToLogin(request,response);
            }
        }
        return false;
    }

    /**
     * 认证成功则把用户信息保存到session中
     * @param token
     * @param subject
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest= (HttpServletRequest) request;
        HttpServletResponse httpServletResponse= (HttpServletResponse) response;
        //subject.getPrincipal获取的是realm返回的simpleAuthenticationInfo存放的Principal
        User user= (User) subject.getPrincipal();
        //此处获得的session并非HttpSession，而是shiro提供的session
        subject.getSession().setAttribute("user",user);
        HttpSession session=httpServletRequest.getSession();
        //在跳转之前把用户信息存放在session中
        session.setAttribute("user",user);
        JSONObject json=new JSONObject();
        if(WebUtil.isAjax(httpServletRequest)&&user.getEmail()!=null){
            json.put("message","登录成功！");
            WebUtil.printJSON(json.toJSONString(),httpServletResponse);
        }else if(WebUtil.isAjax(httpServletRequest)&&user.getEmail()==null){
            json.put("message","登录成功！请您及时绑定邮箱，否则无法重置账号密码！");
            WebUtil.printJSON(json.toJSONString(),httpServletResponse);
        }
        else {
            //跳转到首页，若不设置则还会停留在登录界面
            String indexPath=httpServletRequest.getContextPath()+"/toHome";
            //使用shiro中的WebUtils中的redirectToSavedRequest进行手动跳转
            WebUtils.redirectToSavedRequest(request,response,indexPath);
        }
        return false;
    }

    /**
     * 重写认证失败方法
     * 若为ajax请求则返回json数据
     * 若非ajax请求则默认返回loginCheck处理
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest= (HttpServletRequest) request;
        HttpServletResponse httpServletResponse= (HttpServletResponse) response;
        //若不是ajax请求，则交给原来的方法去处理（返回异常给Controller）
        if (!WebUtil.isAjax(httpServletRequest)){
            setFailureAttribute(request,e);
            return true;
        }
        //获取错误类的名字
        String msg=e.getClass().getSimpleName();
        JSONObject json=new JSONObject();
        if("IncorrectCredentialsException".equals(msg)){
            json.put("message","密码错误！");
            WebUtil.printJSON(json.toJSONString(),httpServletResponse);
        }else if("UnknownAccountException".equals(msg)){
            json.put("message","用户不存在！");
            WebUtil.printJSON(json.toJSONString(),httpServletResponse);
        }else if("CaptchaCodeError".equals(msg)){
            json.put("message","验证码错误！");
            WebUtil.printJSON(json.toJSONString(),httpServletResponse);
        }else if("ConcurrentAccessException".equals(msg)){
            json.put("message","此账号已经是登录状态！");
            WebUtil.printJSON(json.toJSONString(),httpServletResponse);
        }else if("UnknownAccountRoleException".equals(msg)){
            json.put("message","非法账号！");
            WebUtil.printJSON(json.toJSONString(),httpServletResponse);
        }
        return false;
    }

    /**
     * 若用户在登录时点击了“记住我”，那么下次则会自动登录
     * @param request
     * @return
     */
   private boolean autoLogin(HttpServletRequest request){
        Subject currentUser= SecurityUtils.getSubject();
        //若isAuthenitcated为false，则说明不是登录过的，isRemember为true，则说明使用了rememberMe功能
        if(currentUser.isRemembered()){
            User user=(User)SecurityUtils.getSubject().getPrincipal();
            UsernamePasswordToken token=new UsernamePasswordToken(user.getUsername(),user.getPassword(),currentUser.isRemembered());
            currentUser.login(token);
            //把用户放入session中
            Session session=currentUser.getSession();
            session.setAttribute("user",user);
            //设置为永不过期
            session.setTimeout(-10001);
            request.getSession().setAttribute("user",user);
            return true;
        }
        return false;
   }

    /**
     * 登录进入系统前先判断用户密码是否为初始密码
     * @param user 登录的账号信息
     * @return
     * @throws UserException
     */
   private boolean checkAccountPasswordIsDefault(User user) throws AuthenticationException {
       ByteSource byteSource=ByteSource.Util.bytes(user.getUsername());
       String defaultPassword;
       User userInDataBase=baseService.getUserByUserName(user.getUsername());
       if(userInDataBase==null) {
           throw new UnknownAccountException("unknown account");
       }
       if(!userInDataBase.equals(user)){
           throw new IncorrectCredentialsException("username or password error");
       }
       if(userInDataBase.getRole().contains("student")){
           defaultPassword=new SimpleHash("MD5",defaultStudentPassword,byteSource,1024).toString();
           if(defaultPassword.equals(user.getPassword())){
               return true;
           }
           return false;
       }else if(userInDataBase.getRole().contains("teacher")){
           defaultPassword=new SimpleHash("MD5",defaultTeacherPassword,byteSource,1024).toString();
           if(defaultPassword.equals(user.getPassword())){
               return true;
           }
           return false;
       }else if(userInDataBase.getRole().contains("admin")){
           defaultPassword=new SimpleHash("MD5",defaultAdminPassword,byteSource,1024).toString();
           if(defaultPassword.equals(user.getPassword())){
               return true;
           }
           return false;
       }else{
           throw new UnknownAccountRoleException("unknown account role");
       }
   }

}
