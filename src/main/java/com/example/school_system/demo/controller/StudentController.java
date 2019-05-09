package com.example.school_system.demo.controller;

import com.example.school_system.demo.exception.UserException;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.CourseSelectionService;
import com.example.school_system.demo.service.StudentService;
import com.example.school_system.demo.utils.WebUtil;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentController{

    @Autowired
    private StudentService studentService;
    @Autowired
    private CourseSelectionService courseSelectionService;


    /**
     * 获取学生信息
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/info")
    @ResponseBody
    public Student getStudentInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session=request.getSession();
        User user= (User) session.getAttribute("user");
        if(user==null){
            throw new Exception("System exception!(user is null)");
        }
        String id=user.getUsername();
        Student student=studentService.getStudentById(id);
        if(student==null){
            throw new UserException("无法查找到此学生的信息！");
        }
        return student;
    }

    /**
     * 更新个人信息
     * @param sex
     * @param age
     * @param birthday
     * @param addr
     * @param tel
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/info/update")
    public void updateStudentInfo(String sex,String age,String birthday,String addr,String tel,
                                  HttpServletRequest request,HttpServletResponse response) throws Exception {
        HttpSession session=request.getSession();
        User user= (User) session.getAttribute("user");
        if(user==null){
            throw new Exception("System exception!(user is null)");
        }
        String id=user.getUsername();
        Map<String,String> map=new HashMap<String, String>();
        map.put("id",id);
        map.put("sex",sex);
        map.put("birthday",birthday);
        map.put("tel",tel);
        map.put("addr",addr);
        studentService.updateInfoById(map);
        JSONObject json=new JSONObject();
        json.put("message","修改成功！");
        WebUtil.printJSON(json.toJSONString(),response);
    }


    /**
     * 下载学生学籍pdf
     * WebUtil.creatPDF会生成pdf文件，以供下载
     * @param request
     * @param response
     * @throws SAXException
     */
    @RequestMapping("/downloadPdf")
    public void downloadPdf(HttpServletRequest request,HttpServletResponse response) throws SAXException {
        User user= (User) request.getSession().getAttribute("user");
        String id=user.getUsername();
        StudentStatusMsg student_status_msg=studentService.getStudentStatusMsgId(id);
        request.getSession().setAttribute("student_status_msg",student_status_msg);
        String pdfUrl="C:\\Users\\Administrator\\Desktop\\schoolSys\\src\\main\\resources\\templates\\pdf\\student-status-msg\\"+id+".pdf";
        WebUtil.createPDF(pdfUrl,response,request,request.getServletContext());
        File file=new File(pdfUrl);
        if(file.exists()){
            byte[] buffer=new byte[1024];
            try{
                response.setContentType("application/force-download");
                response.addHeader("Content-Disposition","attachment;fileName="+id+".pdf");
                FileInputStream fileInputStream=new FileInputStream(file);
                BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
                OutputStream outputStream=response.getOutputStream();
                int i=bufferedInputStream.read(buffer);
                while(i!=-1){
                    outputStream.write(buffer,0,i);
                    i=bufferedInputStream.read(buffer);
                }
            }catch (IOException e){
                JSONObject json=new JSONObject();
                json.put("message",e.getMessage());
                WebUtil.printJSON(json.toJSONString(),response);
            }
        }
        JSONObject json=new JSONObject();
        json.put("message","done");
        WebUtil.printJSON(json.toJSONString(),response);
    }

    /**
     *获取课程表信息，用于给前端回填数据
     * @param request
     * @param response
     * @param term
     * @return
     */
    @RequestMapping("/timestable")
    @ResponseBody
    public List<TimestableVo> getStudentTimestable(HttpServletRequest request, HttpServletResponse response, String term){
        List<Timestable> timestables=studentService.getTimestableByStudentClass((String) request.getSession().getAttribute("studentClass"));
        if(timestables==null){
            JSONObject json=new JSONObject();
            json.put("message","is null");
            WebUtil.printJSON(json.toJSONString(),response);
        }
        List<TimestableVo> timestableVos =new ArrayList<TimestableVo>();
        for(int i=0;i<timestables.size();i++){
            TimestableVo timestableVo =timestables.get(i).toTimestablePo();
            timestableVos.add(timestableVo);
        }
        return timestableVos;
    }

    /**
     * 预选课程
     * @param response
     * @param request
     * @param courseId
     */
    @RequestMapping("/selectCourse")
    public void selectCourse(HttpServletResponse response,HttpServletRequest request,String courseId){
        HttpSession session=request.getSession();
        User user= (User) session.getAttribute("user");
        String studentId=user.getUsername();
        Long result=courseSelectionService.selectCourse(courseId,studentId);
        JSONObject json=new JSONObject();
        if(result==-1){
            json.put("message","您已经选择过此课程了，不能重复选择！");
            WebUtil.printJSON(json.toJSONString(),response);
            json.clear();
        }
        if(result==0){
            json.put("message","课程可选人数不足，无法选择！");
            WebUtil.printJSON(json.toJSONString(),response);
            json.clear();
        }
        if(result==1){
            json.put("message","预选成功！");
            WebUtil.printJSON(json.toJSONString(),response);
            json.clear();
        }
    }

    /**
     * 取消预选课程
     * @param response
     * @param request
     * @param courseId 课程id
     */
    @RequestMapping("/cancelCourse")
    public void cancelCourse(HttpServletResponse response,HttpServletRequest request,String courseId){
        User user= (User) request.getSession().getAttribute("user");
        String studentId=user.getUsername();
        Long result=courseSelectionService.cancelSelectedCourse(courseId,studentId);
        JSONObject json=new JSONObject();
        if(result==1){
            json.put("message","success");
            WebUtil.printJSON(json.toJSONString(),response);
            json.clear();
        }
        json.put("message","无法取消此课程！");
        WebUtil.printJSON(json.toJSONString(),response);
    }

}
