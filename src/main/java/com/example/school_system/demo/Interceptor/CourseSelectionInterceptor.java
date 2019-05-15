package com.example.school_system.demo.Interceptor;

import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.AdminService;
import com.example.school_system.demo.service.StudentService;
import com.example.school_system.demo.utils.TimeUtil;
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
    @Autowired
    private AdminService adminService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HttpSession session=request.getSession();
        User user= (User) session.getAttribute("user");
        //判断学生所在班级是否可以进行预选课程
        String majorClassId=user.getUsername().substring(0,6);
        List<PreSelectCourseTask> preSelectCourseTasks=adminService.getTaskByCondition("","",majorClassId);
        if(preSelectCourseTasks.size()==0){
            WebUtil.printJSON("您的班级还未开放预选课程功能！",response);
        }
        //若可以预选课程则判断现在是否在预选课程时间段内
        String times=preSelectCourseTasks.get(0).getTime();
        String startTime=times.split("~")[0];
        String endTime=times.split("~")[1];
        if(!TimeUtil.isInTime(startTime,endTime)){
            WebUtil.printJSON("尚未到预选课程时间段！",response);
        }
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
