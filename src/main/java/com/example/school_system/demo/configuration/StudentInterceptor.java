package com.example.school_system.demo.configuration;

import com.example.school_system.demo.pojo.Student_status_msg;
import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.service.StudentService;
import com.example.school_system.demo.utils.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 此过滤器是用于加载学生学籍信息页面前先进行获取数据
 * 由于编译执行thymeleaf比执行jquery还要更早 所以使用$().ready()方法去提前加载数据是无效的
 */
@Component
public class StudentInterceptor implements HandlerInterceptor {

    @Autowired
    private StudentService studentService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user= (User) request.getSession().getAttribute("user");
        String username=user.getUsername();
        Student_status_msg student_status_msg=studentService.getStudentStatusMsgId(username);
        if(student_status_msg==null){
            WebUtil.printJSON("没有相关数据！",response);
        }
        if(student_status_msg.getStudent_img_url()==null){
            student_status_msg.setStudent_img_url("/templates/pdf/student-status-msg/student_img/default.jpg");
        }
        request.getSession().setAttribute("student_status_msg",student_status_msg);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
