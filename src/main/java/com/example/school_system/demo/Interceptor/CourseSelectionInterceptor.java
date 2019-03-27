package com.example.school_system.demo.Interceptor;

import com.example.school_system.demo.pojo.Course;
import com.example.school_system.demo.pojo.CourseVo;
import com.example.school_system.demo.pojo.StudentStatusMsg;
import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.service.StudentService;
import com.example.school_system.demo.utils.WebUtil;
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
public class CourseSelectionInterceptor implements HandlerInterceptor {

    @Autowired
    private StudentService studentService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HttpSession session=request.getSession();
        User user= (User) session.getAttribute("user");
        StudentStatusMsg studentStatusMsg=studentService.getStudentStatusMsgId(user.getUsername());
        List<Course> courses=studentService.getCourseByMajorName(studentStatusMsg.getMajor());
        List<CourseVo> courseVos=new ArrayList<>();
        for(int i=0;i<courses.size();i++){
            if(courses.get(i)==null){
                WebUtil.printJSON("没有可以选择的课程",response);
            }
            CourseVo courseVo=courses.get(i).toCourseVo();
            courseVos.add(courseVo);
        }
        session.setAttribute("courseVo",courseVos);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
