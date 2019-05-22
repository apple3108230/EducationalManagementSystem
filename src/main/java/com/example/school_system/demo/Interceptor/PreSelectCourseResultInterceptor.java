package com.example.school_system.demo.Interceptor;

import com.example.school_system.demo.dao.StudentDao;
import com.example.school_system.demo.pojo.Course;
import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.utils.WebUtil;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PreSelectCourseResultInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private StudentDao studentDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user= (User) request.getSession().getAttribute("user");
        Set<String> courseIds=redisTemplate.opsForSet().members("student:"+user.getUsername());
        List<Course> courseList=new ArrayList<>();
        if(courseIds.size()==0||courseIds==null){
            WebUtil.printToWeb("您还没有预选课程！",response);
        }
        courseIds.forEach(value->{
            Course course=studentDao.getCourseByCourseId(value);
            if(course!=null){
                courseList.add(course);
            }
        });
        if(courseList.size()==0||courseList==null){
            WebUtil.printToWeb("您还没有预选课程！",response);
        }
        request.getSession().setAttribute("courseList",courseList);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
