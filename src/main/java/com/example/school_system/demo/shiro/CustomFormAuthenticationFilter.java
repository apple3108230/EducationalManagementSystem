package com.example.school_system.demo.shiro;

import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.utils.CaptchaUtil;
import com.example.school_system.demo.utils.WebUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.json.simple.JSONObject;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



/**
 * 自定义一个表单认证过滤器 实现自己控制表单认证流程（自动拦截表单中的username和password）
 */
public class CustomFormAuthenticationFilter extends FormAuthenticationFilter {

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
                if (CaptchaUtil.checkCaptchaCode(httpServletRequest, httpServletRequest.getParameter("captchaCode"))) {
                    return executeLogin(request, response);
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

        if(WebUtil.isAjax(httpServletRequest)){
            JSONObject json=new JSONObject();
            json.put("message","登录成功！");
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
            json.clear();
        }else if("UnknownAccountException".equals(msg)){
            json.put("message","用户不存在！");
            WebUtil.printJSON(json.toJSONString(),httpServletResponse);
            json.clear();
        }else if("CaptchaCodeError".equals(msg)){
            json.put("message","验证码错误！");
            WebUtil.printJSON(json.toJSONString(),httpServletResponse);
            json.clear();
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

}
