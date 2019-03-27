package com.example.school_system.demo.Interceptor;

import com.example.school_system.demo.dao.CourseSelectionDao;
import com.example.school_system.demo.dao.StudentDao;
import com.example.school_system.demo.pojo.Course;
import com.example.school_system.demo.pojo.SelectCourseResult;
import com.example.school_system.demo.pojo.StudentStatusMsg;
import com.example.school_system.demo.pojo.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class SelectCourseResultInterceptor implements HandlerInterceptor {

    @Autowired
    private StudentDao studentDao;
    @Autowired
    private CourseSelectionDao courseSelectionDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user= (User) request.getSession().getAttribute("user");
        String studentId=user.getUsername();
        StudentStatusMsg studentStatusMsg=studentDao.getStudentStatusMsgId(studentId);
        List<SelectCourseResult> result=courseSelectionDao.getSelectCourseResult(studentStatusMsg.getMajor());
        List<Course> courses=new ArrayList<>();
        JSONParser parser=new JSONParser();
        result.forEach(val1->{
            try {
                JSONObject json= (JSONObject) parser.parse(val1.getStudentId());
                json.forEach((key,value)->{
                    if(!value.toString().isEmpty()&&value.equals(studentId)){
                        String courseId=val1.getCourseId();
                        courses.add(studentDao.getCourseByCourseId(courseId));
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        request.getSession().setAttribute("courses",courses);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
