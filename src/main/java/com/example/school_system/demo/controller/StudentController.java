package com.example.school_system.demo.controller;

import com.example.school_system.demo.exception.UserException;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.StudentService;
import com.example.school_system.demo.utils.WebUtil;
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
        StudentStatusMsg student_status_msg=studentService.getStudentStatusMsgId(user.getUsername());
        if(student_status_msg==null){
            WebUtil.printJSON("没有相关数据！",response);
        }
        session.setAttribute("student_status_msg",student_status_msg);
        WebUtil.printJSON("success",response);
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
                WebUtil.printJSON(e.getMessage(),response);
            }
        }
        WebUtil.printJSON("done",response);
    }

    @RequestMapping("/timestable")
    @ResponseBody
    public List<TimestablePo> getStudentTimestable(HttpServletRequest request,HttpServletResponse response,String term){
        List<Timestable> timestables=studentService.getTimestableByStudentClass((String) request.getSession().getAttribute("studentClass"));
        if(timestables==null){
            WebUtil.printJSON("is null",response);
        }
        List<TimestablePo> timestablePos=new ArrayList<TimestablePo>();
        for(int i=0;i<timestables.size();i++){
            TimestablePo timestablePo=timestables.get(i).toTimestablePo();
            timestablePos.add(timestablePo);
        }
        return timestablePos;
    }
}
