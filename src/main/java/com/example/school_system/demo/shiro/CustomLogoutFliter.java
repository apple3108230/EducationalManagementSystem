package com.example.school_system.demo.shiro;

import com.example.school_system.demo.utils.WebUtil;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.json.simple.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * 重写shiro中的logoutFilter 退出登录前先进行清除个人信息（清除shiro中的session和servlet中的session、cookie信息），然后重定向至登录页面
 *
 */
public class CustomLogoutFliter extends LogoutFilter {

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        Subject subject=getSubject(request,response);
        ServletContext context=request.getServletContext();
        String redirectUrl=getRedirectUrl(request,response,subject);
        //清除servlet中的session和退出登录
        try{
            context.removeAttribute("user");
            //会移除shiro中的session以及web中的所有有关联的session以及cookie
            subject.logout();
        }catch (SessionException e){
            throw new Exception("退出失败！原因："+e.getMessage());
        }
        issueRedirect(request,response,redirectUrl);
        return false;
    }
}
