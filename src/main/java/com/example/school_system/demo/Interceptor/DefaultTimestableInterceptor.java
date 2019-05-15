package com.example.school_system.demo.Interceptor;

import com.example.school_system.demo.dao.TimestableDao;
import com.example.school_system.demo.pojo.Student;
import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultTimestableInterceptor implements HandlerInterceptor {

    @Autowired
    private StudentService studentService;
    @Autowired
    private TimestableDao timestableDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HttpSession session=request.getSession();
        List<String> terms=timestableDao.getAllTerm();
        session.setAttribute("terms",terms);
//        User user= (User) session.getAttribute("user");
//        Student student=studentService.getStudentById(user.getUsername());
//        session.setAttribute("studentClass",student.getStudentClass());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
