package com.example.school_system.demo.controller;

import com.example.school_system.demo.dao.TeacherDao;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.ExcelService;
import com.example.school_system.demo.service.LogService;
import com.example.school_system.demo.service.TeacherService;
import com.example.school_system.demo.service.TimestableService;
import com.example.school_system.demo.utils.ScoreUtil;
import com.example.school_system.demo.utils.StringUtil;
import com.example.school_system.demo.utils.TimeUtil;
import com.example.school_system.demo.utils.WebUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Time;
import java.util.*;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private TimestableService timestableService;
    @Autowired
    private ExcelService excelService;
    @Autowired
    private TeacherDao teacherDao;
    @Autowired
    private LogService logService;

    @Value("${excel.save-path}")
    private String excelSavePath;

    public Map<String,String> parseQueryScoreCondition(User user,QueryScoreCondition queryScoreCondition) throws IllegalAccessException {
        Map<String,String> conditionMap=new HashMap<>();
        Class cls=queryScoreCondition.getClass();
        Field[] fields=cls.getDeclaredFields();
        for(Field field:fields){
            //在反射private成员时 需要将字段设置为可访问的
            field.setAccessible(true);
            String key=field.getName();
            String value= (String) field.get(queryScoreCondition);
            switch (key){
                case "studentIdCondition":
                    if(value!=null&&!value.isEmpty()){
                        conditionMap.put("student_id",value);
                    }
                    break;
                case "pageNum":
                    if(value!=null&&!value.isEmpty()){
                        //默认页面显示条数为20条
                        PageHelper.startPage(Integer.parseInt(value),20);
                    }
                    break;
                case "otherCondition":
                    if(value!=null&&!value.isEmpty()&&value.equals("OnlyWatchElectiveCourse")){
                        conditionMap.put("course_type","选修");
                    }
                    if(value!=null&&!value.isEmpty()&&value.equals("OnlyWatchCompulsorySubjects")){
                        conditionMap.put("course_type","必修");
                    }
                    if(value!=null&&!value.isEmpty()&&value.equals("OnlyWatchOwnAcademy")){
                        String academy=user.getUsername().substring(0,2);
                        conditionMap.put("student_id",academy+"%");
                    }
                    break;
                case "classIdCondition":
                    if(value!=null&&!value.isEmpty()){
                        conditionMap.put("student_id",value.substring(0,7)+"%");
                    }
                    break;
                default:
                    if(value!=null&&!value.isEmpty()){
                        String[] splitStr=key.split("C");
                        String condition=splitStr[0];
                        conditionMap.put(condition,value);
                    }
            }
        }
        return conditionMap;
    }


    @RequestMapping("/info")
    @ResponseBody
    public TeacherMsg getTeacherMsgById(HttpServletRequest request){
        User user= (User) request.getSession().getAttribute("user");
        TeacherMsg msg=teacherService.getTeacherMsgById(user.getUsername());
        return msg;
    }

    @RequestMapping("/getClassTimestable")
    @ResponseBody
    public List<TimestableVo> getClassTimestable(String classId){
        List<Timestable> timestables=timestableService.getClassTimestable(classId);
        List<TimestableVo> timestableVoList=new ArrayList<>();
        timestables.forEach(value->{
            TimestableVo timestableVo=value.toTimestablePo();
            timestableVoList.add(timestableVo);
        });
        return timestableVoList;
    }

    @RequestMapping("/getClassRoomTimestable")
    @ResponseBody
    public List<TimestableVo> getClassRoomTimestable(String classroomName){
        List<Timestable> timestableList=timestableService.getClassRoomTimestable(classroomName);
        List<TimestableVo> timestableVoList=new ArrayList<>();
        timestableList.forEach(value->{
            TimestableVo timestableVo=value.toTimestablePo();
            timestableVoList.add(timestableVo);
        });
        return timestableVoList;
    }

    @RequestMapping("/getTimestableByTeacherNameAndTerm")
    @ResponseBody
    public List<TimestableVo> getTimestableByTeacherNameAndTerm(HttpServletRequest request,String term){
        User user= (User) request.getSession().getAttribute("user");
        TeacherMsg msg=teacherService.getTeacherMsgById(user.getUsername());
        List<Timestable> timestableList=timestableService.getTimestableByTeacherNameAndTerm(msg.getName(),term);
        List<TimestableVo> timestableVoList=new ArrayList<>();
        timestableList.forEach(value->{
            TimestableVo timestableVo=value.toTimestablePo();
            timestableVoList.add(timestableVo);
        });
        return timestableVoList;
    }

    @RequestMapping("/uploadFile")
    public void uploadFile(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
        List<Part> parts=(List<Part>)request.getParts();
        List<String> fileNames=new ArrayList<>();
        parts.forEach(part->{
            String header=part.getHeader("Content-Disposition");
            int start = header.lastIndexOf("=");
            String fileName = header.substring(start + 1)
                    .replace("\"", "");
            fileNames.add(fileName);
            if(fileName!=null&&!fileName.isEmpty()){
                try {
                    part.write(excelSavePath+"/"+fileName);
                } catch (IOException e) {
                    JSONObject json=new JSONObject();
                    json.put("message","上传失败！原因："+e.getMessage());
                    WebUtil.printJSON(json.toJSONString(),response);
                }
            }
        });
        excelService.resolveExcelAndInsertScore(response,request,fileNames);
    }

    @PostMapping("/checkScoreExist")
    public void checkScoreExist(HttpServletResponse response,@RequestBody StudentScorePo studentScorePo){
        String studentId=studentScorePo.getStudentId();
        String course=studentScorePo.getCourse();
        String term=studentScorePo.getTerm();
        int result=teacherDao.checkScoreExist(studentId,course,term);
        JSONObject json=new JSONObject();
        if(result>=1){
            json.put("message","已经存在此成绩记录！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","success!");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }


    @PostMapping("/uploadScores")
    public void uploadScores(HttpServletResponse response, @RequestBody List<StudentScorePo> scores,HttpServletRequest request){
        List<StudentScore> studentScores=new ArrayList<>();
        scores.forEach(score->{
            String totalScore=ScoreUtil.countTotalScore(score.getUsualScore(),score.getExamScore());
            String gpa=ScoreUtil.countGPA(totalScore);
            String creditGpa=ScoreUtil.countCreditGpa(score.getCredit(),gpa);
            StudentScore studentScore=score.toStudentScore(totalScore,gpa,creditGpa);
            studentScore.setId(StringUtil.CustomUUID());
            studentScores.add(studentScore);
        });
        boolean i=teacherService.insertScoreByStudentId(studentScores);
        JSONObject json=new JSONObject();
        if(i){
            //上传至操作日志
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            StringBuffer allStudentId=new StringBuffer();
            scores.forEach(score->{
                allStudentId.append(score.getStudentId()).append(" ");
            });
            sensitiveOperation.setAction("上传成绩：学号："+allStudentId);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","上传成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","上传失败！请联系管理员");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @PostMapping("/queryScore")
    @ResponseBody
    public List queryScore(@RequestBody QueryScoreCondition queryScoreCondition,HttpServletRequest request) throws IllegalAccessException {
        User user= (User) request.getSession().getAttribute("user");
        Map<String,String> conditionMap=parseQueryScoreCondition(user,queryScoreCondition);
        List<StudentScore> studentScores=teacherDao.selectStudentScoresByCondition(conditionMap);
        PageInfo<StudentScore> info=new PageInfo<>(studentScores);
        int totalPage=info.getPages();
        Map map=new HashMap();
        map.put("totalPage",String.valueOf(totalPage));
        List jsonList=new ArrayList();
        jsonList.add(map);
        jsonList.add(studentScores);
        return jsonList;
    }

    @PostMapping("/updateScore")
    public void updateScore(@RequestBody StudentScore studentScore,HttpServletResponse response,HttpServletRequest request){
        boolean isUpdate=teacherService.updateStudentScoreByCourseAndStudentId(studentScore);
        JSONObject json=new JSONObject();
        if(isUpdate){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("上传成绩：学号："+studentScore.getStudentId());
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","修改成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","修改失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @PostMapping("/deleteScore")
    //由于参数中不可以使用多个@RequestBody来解析json中的每个键值对 所以只能解析成pojo类
    public void deleteScore(@RequestBody DeleteScoreInfo deleteScoreInfo, HttpServletResponse response,HttpServletRequest request){
        boolean isDelete=teacherService.deleteStudentScoreByCourseAndStudentId(deleteScoreInfo);
        JSONObject json=new JSONObject();
        if(isDelete){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("删除成绩：学号："+deleteScoreInfo.getStudentId());
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","删除成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","删除失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @PostMapping("/getNextPage")
    @ResponseBody
    public List getNextPage(@RequestBody QueryScoreCondition queryScoreCondition,HttpServletRequest request) throws IllegalAccessException {
        User user= (User) request.getSession().getAttribute("user");
        Map<String,String> conditionMap=parseQueryScoreCondition(user,queryScoreCondition);
        List<StudentScore> studentScores=teacherDao.selectStudentScoresByCondition(conditionMap);
        PageInfo<StudentScore> info=new PageInfo<>(studentScores);
        int totalPage=info.getPages();
        List jsonList=new ArrayList();
        Map map=new HashMap();
        map.put("totalPage",String.valueOf(totalPage));
        jsonList.add(map);
        jsonList.add(studentScores);
        return jsonList;
    }
}
