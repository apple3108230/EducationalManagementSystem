package com.example.school_system.demo.shiro;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;


@Configuration
/**
 *
 * shiro的配置类
 *
 */
public class ShiroConfigurtion {

    /**
     * 自定义的realm
     * 使用MD5进行密码加密 加密次数为1024次
     * @return
     */
    @Bean
    public ShiroRealm myShiroRealm(){
        ShiroRealm myShiroRealm=new ShiroRealm();
        HashedCredentialsMatcher matcher=new HashedCredentialsMatcher();
        matcher.setHashIterations(1024);
        matcher.setHashAlgorithmName("MD5");
        myShiroRealm.setCredentialsMatcher(matcher);
        return myShiroRealm;
    }

    /**
     * session管理器
     * SessionIdUrlRewritingEnabled 设置为禁止重写sessionId
     * GlobalSessionTimeout 设置session的失效时间为700000（单位为毫秒）
     * DeleteInvalidSessions 设置为可以删除失效的session
     * @return
     */
    @Bean
    public DefaultWebSessionManager webSessionManager(){
        DefaultWebSessionManager webSessionManager=new DefaultWebSessionManager();
        webSessionManager.setSessionIdUrlRewritingEnabled(false);
        webSessionManager.setGlobalSessionTimeout(700000);
        webSessionManager.setDeleteInvalidSessions(true);
        return webSessionManager;
    }

    //配置SecurityManager
    @Bean
    public SecurityManager securityManager(){
        DefaultWebSecurityManager securityManager=new DefaultWebSecurityManager();
        securityManager.setRealm(myShiroRealm());
        securityManager.setRememberMeManager(cookieRememberMeManager());
        securityManager.setSessionManager(webSessionManager());
        return securityManager;
    }

    /**
     * shiro过滤器
     * 需要注意的是 setSuccessUrl只是做为一个附加配置，只有session中没有用户请求地址时才是用我们设置的successUrl。系统默认认证成功后跳转到上一次请求的路径。
     * 所以我在login页面加入了登陆成功跳转
     * @param securityManager
     * @return
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean=new ShiroFilterFactoryBean();
        Map<String, Filter> filterMap=shiroFilterFactoryBean.getFilters();
        //这里不使用默认的FormAuthenticationFilter，使用自定义的customFormAuthenticationFilter进行表单验证
        filterMap.put("authc",customFormAuthenticationFilter());
        filterMap.put("logout",logoutFliter());
        shiroFilterFactoryBean.setFilters(filterMap);
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setLoginUrl("/loginCheck");
        //在shiro中 登录成功后是默认返回到登录的上一个页面 setSuccessUrl只能起一个辅助作用 当shiro的session中没有用户请求地址才会使用此方法定义的url
        shiroFilterFactoryBean.setSuccessUrl("/to/home");
        //权限控制
        //Map<String,String> filterMap=new HashMap<String, String>() 不使用Map而是使用LinkedHashMap是因为保证有序;过滤顺序是从上往下
        LinkedHashMap<String,String> filterChainMap=new LinkedHashMap<String, String>();
        filterChainMap.put("templates/img/**","anon");
        filterChainMap.put("templates/js/**","anon");
        filterChainMap.put("templates/css/**","anon");
        filterChainMap.put("templates/pdf/student-status-msg/student_img/**","authc");
        filterChainMap.put("/kaptchaCode","anon");
        filterChainMap.put("/sendMail","anon");
        filterChainMap.put("/sendMailAgain","anon");
        filterChainMap.put("/home","anon");
        //退出登录拦截器
        filterChainMap.put("/logout","logout");
        filterChainMap.put("/loginCheck","authc");
        filterChainMap.put("/to/home/**","authc");
        filterChainMap.put("/to/home","authc");
        filterChainMap.put("/to/**","anon");
        filterChainMap.put("/**","anon");
        //登录页面可以匿名访问
        //filterChainMap.put("/**","user");
        //记住我或者通过认证即可访问所有url
//        filterChainMap.put("/**","user");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainMap);
        return shiroFilterFactoryBean;
    }

//    @Bean
//    public FilterRegistrationBean filterRegistrationBean(){
//        FilterRegistrationBean filterRegistrationBean=new FilterRegistrationBean();
//        DelegatingFilterProxy delegatingFilterProxy=new DelegatingFilterProxy();
//        delegatingFilterProxy.setTargetFilterLifecycle(true);
//        delegatingFilterProxy.setTargetBeanName("shiroFilter");
//        filterRegistrationBean.setFilter(delegatingFilterProxy);
//        return filterRegistrationBean;
//    }

    /**
     * 设置rememberMeCookie
     * cookie名称为rememberMe
     * MaxAge cookie的有效时间为86400（单位为秒）
     * @return
     */
    @Bean
    public SimpleCookie rememberMe(){
        SimpleCookie simpleCookie=new SimpleCookie("rememberMe");
        simpleCookie.setMaxAge(86400);
        return simpleCookie;
    }

    //负责写cookie和读取cookie中的用户信息
    @Bean
    public CookieRememberMeManager cookieRememberMeManager(){
        CookieRememberMeManager cookieRememberMeManager=new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMe());
        return cookieRememberMeManager;
    }

    /**
     * 自定义表单验证过滤器
     * UsernameParam 对应表单中的用户名的input的名称
     * PasswordParam 对应表单中的密码的input的名称
     * RememberMeParam
     * @return
     */
    @Bean
    CustomFormAuthenticationFilter customFormAuthenticationFilter(){
        CustomFormAuthenticationFilter customFormAuthenticationFilter=new CustomFormAuthenticationFilter();
        customFormAuthenticationFilter.setUsernameParam("username");
        customFormAuthenticationFilter.setPasswordParam("password");
        customFormAuthenticationFilter.setRememberMeParam("rememberMe");
        return customFormAuthenticationFilter;
    }

    /**
     * 这里不加@Bean 是因为不想交给spring管理 若使用了@Bean 则会引起过滤器顺序问题
     * setRedirectUrl logout后重定向的页面
     * @return
     */
    public CustomLogoutFliter logoutFliter(){
        CustomLogoutFliter fliter=new CustomLogoutFliter();
        fliter.setRedirectUrl("/to/login");
        return fliter;
    }

//    @Bean
//    public LogoutFilter logoutFilter(){
//        LogoutFilter logoutFilter=new LogoutFilter();
//        logoutFilter.setRedirectUrl("/home");
//        return logoutFilter;
//      }
}
