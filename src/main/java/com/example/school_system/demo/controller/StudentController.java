package com.example.school_system.demo.controller;

import com.example.school_system.demo.exception.UserException;
import com.example.school_system.demo.pojo.Student;
import com.example.school_system.demo.pojo.Student_status_msg;
import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.service.StudentService;
import com.example.school_system.demo.utils.WebUtil;
import com.lowagie.text.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController extends BaseController {

    @Autowired
    private StudentService studentService;

    /**
     * 获取学生信息
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/info")
    @ResponseBody
    public Student getStudnetInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
        map.put("age",age);
        map.put("tel",tel);
        map.put("addr",addr);
        studentService.updateInfoById(map);
        WebUtil.printJSON("修改成功！",response);
    }

    /**
     * 通过$().ready()进行加载数据是行不通 所以放弃此方法
     * @deprecated
     * @param request
     * @param response
     */
    @RequestMapping("/student_status/info")
    public void getStudentStatusMsg(HttpServletRequest request,HttpServletResponse response){
        HttpSession session=request.getSession();
        User user= (User) session.getAttribute("user");
        Student_status_msg student_status_msg=studentService.getStudentStatusMsgId(user.getUsername());
        if(student_status_msg==null){
            WebUtil.printJSON("没有相关数据！",response);
        }
        session.setAttribute("student_status_msg",student_status_msg);
        WebUtil.printJSON("success",response);
    }

    @RequestMapping("/downloadPdf")
    public void downloadPdf(HttpServletRequest request,HttpServletResponse response) throws SAXException {
        User user= (User) request.getSession().getAttribute("user");
        String id=user.getUsername();
        Student_status_msg student_status_msg=studentService.getStudentStatusMsgId(id);
        request.getSession().setAttribute("student_status_msg",student_status_msg);
        //String htmlUrl="C:\\Users\\Administrator\\Desktop\\schoolSys\\src\\main\\resources\\templates\\page\\studentPage\\student_status_msg.html";
        String pdfUrl="C:\\Users\\Administrator\\Desktop\\schoolSys\\src\\main\\resources\\templates\\pdf\\student-status-msg\\img\\"+id+".pdf";
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
                WebUtil.printJSON(e.getMessage(),response);
            }
        }
        WebUtil.printJSON("done",response);
    }

}
