package com.example.school_system.demo.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 在生成pdf文件之前，使用thymeleaf对模板进行数据渲染
 */
public class GenerateDataForHtml {
    /**
     *
     * @param pdfUrl 生成pdf的位置
     * @param templateName 模板名
     * @param request HttpServletRequest
     * @param response HttpServletRespones
     * @param servletContext ServletContext
     * @return 返回html的字符串
     * @throws IOException
     */
    protected static String generate(String pdfUrl, String templateName,HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws IOException {
        //这里需要获取一个ApplicationContext并注入SpringResourceTemplateResolver中，否则会出现application context cannot null的异常
        ApplicationContext applicationContext= WebApplicationContextUtils.getWebApplicationContext(servletContext);
        //thymeleaf的模板解析器
        SpringResourceTemplateResolver templateResolver=new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setApplicationContext(applicationContext);
        //thymeleaf模板引擎
        SpringTemplateEngine templateEngine=new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        //允许使用SpringEL表达式
        templateEngine.setEnableSpringELCompiler(true);
        StringWriter stringWriter=new StringWriter();
        BufferedWriter writer=new BufferedWriter(stringWriter);
        //进行渲染数据
        templateEngine.process(templateName,new WebContext(request,response,servletContext),writer);
        stringWriter.flush();
        stringWriter.close();
        writer.flush();
        writer.close();
        String htmlString=stringWriter.toString();
        return htmlString;
    }
}
