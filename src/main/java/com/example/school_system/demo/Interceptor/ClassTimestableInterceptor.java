package com.example.school_system.demo.Interceptor;

import com.example.school_system.demo.dao.TimestableDao;
import com.example.school_system.demo.utils.StringUtil;
import com.example.school_system.demo.utils.WebUtil;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class ClassTimestableInterceptor implements HandlerInterceptor {

    @Autowired
    private TimestableDao timestableDao;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        List<String> classIds=timestableDao.getAllClassId();
        List<String> classIdsToWeb= new ArrayList<>();
        classIds.forEach(value->{
            classIdsToWeb.add(value);
        });
        request.getSession().setAttribute("classIds",classIdsToWeb);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
