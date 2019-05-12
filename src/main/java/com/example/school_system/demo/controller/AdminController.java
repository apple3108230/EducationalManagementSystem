package com.example.school_system.demo.controller;

import com.example.school_system.demo.dao.AdminDao;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.*;
import com.example.school_system.demo.service.Impl.CourseSelectionServiceImpl;
import com.example.school_system.demo.utils.TimeUtil;
import com.example.school_system.demo.utils.WebUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.json.simple.JSONObject;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminDao adminDao;
    @Autowired
    private BaseService baseService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private CourseSelectionService courseSelectionService;
    @Autowired
    private MajorService majorService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private ClassroomService classroomService;
    @Autowired
    private MajorClassService majorClassService;
    @Autowired
    private ExcelService excelService;
    @Autowired
    private LogService logService;

    @Value("${excel.save-path}")
    private String excelSavePath;
    @Value("${student.account.original-password}")
    private String STUDENT_ORIGINAL_PASSWORD;
    @Value("${teacher.account.original-password}")
    private String TEACHER_ORIGINAL_PASSWORD;
    @Value("${admin.account.original-password}")
    private String ADMIN_ORIGINAL_PASSWORD;

    /**
     * 解析查询拥有此角色用户的条件
     * @param queryRoleCondition 拥有此角色用户的条件
     * @return
     * @throws Exception
     */
    public Map<String,String> parseQueryRoleCondition(QueryRoleCondition queryRoleCondition) throws Exception {
        Class cls=queryRoleCondition.getClass();
        Field[] fields=cls.getDeclaredFields();
        Map<String,String> conditionMap=new HashMap<>();
        for(Field field:fields){
            //在反射private成员时 需要将字段设置为可访问的
            field.setAccessible(true);
            String key=field.getName();
            String value= (String) field.get(queryRoleCondition);
            switch(key){
                case "username":
                    if(value!=null&&!value.isEmpty()){
                        conditionMap.put(key,value);
                    }
                    break;
                case "otherCondition":
                    if(value!=null&&!value.isEmpty()&&value.equals("OnlyWatchRoleIsTeacher")){
                        conditionMap.put("role","%teacher%");
                    }
                    if(value!=null&&!value.isEmpty()&&value.equals("OnlyWatchRoleIsAdmin")){
                        conditionMap.put("role","%admin%");
                    }
                    if(value!=null&&!value.isEmpty()&&value.equals("OnlyWatchRoleIsTeacherAdmin")){
                        conditionMap.put("role","%teacherAdmin%");
                    }
                    break;
                case "pageNum":
                    if (value != null && !value.isEmpty()) {
                        PageHelper.startPage(Integer.parseInt(value),20);
                    }
                    break;
                    default:
                        throw new Exception("parse error");

            }
        }
        return conditionMap;
    }

    /**
     * 重置账号密码以及其绑定邮箱，重置后密码为初始密码，邮箱为空
     * @param username 用户名
     * @param checkboxValue 是否重置绑定邮箱
     * @param response
     */
    @RequestMapping("/resetUser")
    public void resetUser(String username,@RequestParam(required = false) String checkboxValue,HttpServletResponse response,HttpServletRequest request){
        JSONObject json=new JSONObject();
        User user=baseService.getUserByUserName(username);
        if(user==null){
            json.put("message","重置失败！原因（无此用户！）");
            WebUtil.printJSON(json.toJSONString(),response);
        }
        ByteSource source=ByteSource.Util.bytes(username);
        String salt=source.toString();
        int hashInterations=1024;
        String password="";
        //根据用户角色进行重置密码
        if(user.getRole().startsWith("student")){
            password=(new SimpleHash("MD5",STUDENT_ORIGINAL_PASSWORD,source,hashInterations)).toString();
        }else if(user.getRole().startsWith("teacher")){
            password=(new SimpleHash("MD5",TEACHER_ORIGINAL_PASSWORD,source,hashInterations)).toString();
        }else if(user.getRole().startsWith("admin")){
            password=(new SimpleHash("MD5",ADMIN_ORIGINAL_PASSWORD,source,hashInterations)).toString();
        }
        Map<String,String> info=new HashMap<>();
        info.put("username",username);
        info.put("salt",salt);
        info.put("password",password);
        if(checkboxValue!=null){
            info.put("resetBindEmail","true");
        }
        int result=adminDao.updatePasswordByUsername(info);
        if(result>=0){
            User user1= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("重置账号："+username);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user1.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","重置成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","重置失败！系统错误！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    /**
     * 创建管理员账号
     * @param response
     */
    @GetMapping("/creditAdminAccount")
    public void creditAdminAccount(HttpServletResponse response,HttpServletRequest request){
        User user=adminService.creditAdminAccount();
        JSONObject json=new JSONObject();
        if(user!=null){
            User user1= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("创建管理员账号： 所创建的账号："+user.getUsername());
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user1.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","success");
            json.put("account",user.getUsername());
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","创建失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    /**
     * 查询账号的所有角色
     * @param queryRoleCondition 角色条件
     * @return
     * @throws Exception
     */
    @PostMapping("/queryRole")
    @ResponseBody
    public List queryRole(@RequestBody QueryRoleCondition queryRoleCondition) throws Exception {
        Map<String,String> conditionMap=parseQueryRoleCondition(queryRoleCondition);

        List<Role> roleList=adminDao.selectUserRoleByCondition(conditionMap);
        PageInfo<Role> info=new PageInfo<>(roleList);
        int totalPage=info.getPages();
        Map map=new HashMap();
        map.put("totalPage",totalPage);
        List jsonList=new ArrayList();
        jsonList.add(map);
        jsonList.add(roleList);
        return jsonList;
    }

    /**
     * 更改账号角色
     * @param role 角色
     * @param response
     */
    @PostMapping("/updateRole")
    public void updateRole(@RequestBody Role role,HttpServletResponse response,HttpServletRequest request){
        role.getRole().replace(" ",",");
        boolean isUpdate=adminService.updateUserRoleByUsername(role);
        JSONObject json=new JSONObject();
        if (isUpdate) {
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("更新账号角色： 用户："+role.getUsername()+" 更改角色为："+role.getRole());
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","修改成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","修改失败！");
        }
    }

    /**
     * 上传预选任务（一键开启模式）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param response
     * @throws Exception
     */
    @GetMapping("/uploadTaskForSuperMode")
    public void uploadTaskForSuperMode(String startTime,String endTime,HttpServletResponse response,HttpServletRequest request){
        courseSelectionService.uploadTaskForSuperMode(startTime, endTime, response,request);
    }

    /**
     * 上传预选任务（自定义模式）
     * @param majorId 专业id
     * @param classId 班级id
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param response
     * @throws Exception
     */
    @GetMapping("/uploadTaskForCustomMode")
    public void uploadTaskForCustomMode(String majorId,String classId,String startTime,String endTime,HttpServletResponse response,HttpServletRequest request) throws Exception {
        courseSelectionService.uploadTaskForCustomMode(majorId, classId, startTime, endTime, response,request);
    }

    /**
     * 获取预选任务
     * @param academyNameCondition 学院名
     * @param majorNameCondition 专业名
     * @param classIdCondition 班级名
     * @param pageNum 页数
     * @return
     */
    @GetMapping("/getTasks")
    @ResponseBody
    public List getTasks(String academyNameCondition,String majorNameCondition,String classIdCondition,String pageNum) throws SchedulerException {
        PageHelper.startPage(Integer.parseInt(pageNum),20);
        Map infoMap=new HashMap();
        if(academyNameCondition.isEmpty()&&majorNameCondition.isEmpty()&&classIdCondition.isEmpty()){
            if(checkAfterSelectedCourseTaskExist()){
                infoMap.put("useSuperMode",true);
            }
            infoMap.put("useSuperMode",false);
        }
        List<PreSelectCourseTask> preSelectCourseTasks=adminService.getTaskByCondition(academyNameCondition, majorNameCondition, classIdCondition);
        PageInfo<PreSelectCourseTask> info=new PageInfo<>(preSelectCourseTasks);
        infoMap.put("totalPage",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(infoMap);
        jsonList.add(preSelectCourseTasks);
        return jsonList;
    }

    /**
     * 判断是否存在一键开启预选模式功能的任务
     * @return
     * @throws SchedulerException
     */
    @GetMapping("/checkAfterSelectedCourseTaskExist")
    @ResponseBody
    public boolean checkAfterSelectedCourseTaskExist() throws SchedulerException {
        return taskService.checkTaskExist(CourseSelectionServiceImpl.PRE_SELECT_COURSE_JOB_NAME_FOR_SUPER_MODE,CourseSelectionServiceImpl.JOB_GROUP_NAME);
    }

    /**
     * 删除预选任务
     * @param className
     * @param response
     */
    @GetMapping("/deleteTask")
    public void deleteTask(String className,HttpServletResponse response,HttpServletRequest request){
        JSONObject json=new JSONObject();
        if(adminService.deleteTaskByClassName(className)){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("删除预选任务：班级："+className);
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

    @GetMapping("/deleteAllSuperModeTask")
    public void deleteAllSuperModeTask(HttpServletResponse response,HttpServletRequest request){
        boolean isDelete=adminService.deleteAllSuperModeTask();
        JSONObject json=new JSONObject();
        if(isDelete){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("关闭一键开启预选功能");
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","success");
            WebUtil.printJSON(json.toJSONString(),response);
        }else {
            json.put("message","fail");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    /**
     * 获取敏感操作日志
     * @return
     */
    @GetMapping("/getSensitiveOperationLog")
    @ResponseBody
    public List getSensitiveOperationLog(@RequestParam(value = "pageNum",required = false) String pageNum){
        if(pageNum!=""){
            PageHelper.startPage(Integer.parseInt(pageNum),20);
        }else{
            PageHelper.startPage(1,20);
        }
        List<SensitiveOperation> sensitiveOperations=logService.getSensitiveOperationLog();
        PageInfo info=new PageInfo<>(sensitiveOperations);
        Map map=new HashMap();
        map.put("totalPage",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(map);
        jsonList.add(sensitiveOperations);
        return jsonList;
    }

    /**
     * 获取系统异常日志
     * @return
     */
    @GetMapping("/getSystemErrorLog")
    @ResponseBody
    public List getSystemErrorLog(@RequestParam(value = "pageNum",required = false) String pageNum){
        if(pageNum!=""){
            PageHelper.startPage(Integer.parseInt(pageNum),20);
        }else{
            PageHelper.startPage(1,20);
        }
        List<SystemError> systemErrorList=logService.getSystemError();
        PageInfo info=new PageInfo<>(systemErrorList);
        Map map=new HashMap();
        map.put("totalPage",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(map);
        jsonList.add(systemErrorList);
        return jsonList;
    }

    /**
     * 此方法用于开启定时任务
     * @param time 开启业务时间
     * @param response HTTPServletResponse
     * @throws SchedulerException
     */
    @GetMapping("/startScheduleJob")
    @SuppressWarnings("unchecked")
    public void startScheduleJob(HttpServletResponse response,HttpServletRequest request,String time,String jobName,String scheduleTaskName,String className,@RequestParam(value = "cronExpression",required = false) String cronExpression) throws SchedulerException, SQLException, ClassNotFoundException {
        ScheduleTask scheduleTask=new ScheduleTask();
        if(cronExpression!=""&&time==""){
            scheduleTask.setCronExpression(cronExpression);
        }
        scheduleTask.setTime(time);
        scheduleTask.setCronExpression(TimeUtil.ParseTimeMapToSimpleCronExpression(TimeUtil.parseTimeString(time)));
        scheduleTask.setScheduleTask(scheduleTaskName);
        scheduleTask.setJobName(jobName);
        scheduleTask.setJobGroup(CourseSelectionServiceImpl.JOB_GROUP_NAME);
        scheduleTask.setClassName(className);
        taskService.addTask(scheduleTask);
        User user= (User) request.getSession().getAttribute("user");
        SensitiveOperation sensitiveOperation=new SensitiveOperation();
        sensitiveOperation.setAction("开启定时任务：任务名："+scheduleTaskName+" 时间："+time);
        sensitiveOperation.setTime(TimeUtil.getNowTime());
        sensitiveOperation.setOperator(user.getUsername());
        logService.insertSensitiveOperationLog(sensitiveOperation);
        JSONObject json=new JSONObject();
        json.put("message","任务启动成功！");
        WebUtil.printJSON(json.toJSONString(),response);
    }

    /**
     * 删除定时任务
     * @param response
     * @param scheduleTaskName 定时任务名（数据库）
     * @throws SchedulerException
     * @throws SQLException
     */
    @GetMapping("/deleteScheduleJob")
    public void deleteScheduleJob(HttpServletResponse response,HttpServletRequest request,String scheduleTaskName,@RequestParam(value = "time",required = false) String time,String cronExpression) throws SchedulerException, SQLException {
        Map<String,String> condition=new HashedMap<>();
        condition.put("schedule_task",scheduleTaskName);
        if(time.isEmpty()){
            condition.put("cron_expression",cronExpression);
        }else{
            condition.put("time",time);
        }
        ScheduleTask scheduleTask=taskService.getScheduleTaskByCondition(condition);
        taskService.deleteTask(scheduleTask);
        User user= (User) request.getSession().getAttribute("user");
        SensitiveOperation sensitiveOperation=new SensitiveOperation();
        sensitiveOperation.setAction("删除定时任务：任务名："+scheduleTaskName);
        sensitiveOperation.setTime(TimeUtil.getNowTime());
        sensitiveOperation.setOperator(user.getUsername());
        logService.insertSensitiveOperationLog(sensitiveOperation);
        JSONObject json=new JSONObject();
        json.put("message","任务删除成功！");
        WebUtil.printJSON(json.toJSONString(),response);
    }

    /**
     * 修改定时任务
     * @param response
     * @param scheduleTaskName 定时任务名（数据库）
     * @param time 任务开始时间
     * @throws SchedulerException
     */
    @GetMapping("/editScheduleJob")
    public void editScheduleJob(HttpServletResponse response,HttpServletRequest request,String scheduleTaskName,String time) throws SchedulerException {
        Map<String,String> condition=new HashedMap<>();
        condition.put("schedule_task",scheduleTaskName);
        ScheduleTask scheduleTask=taskService.getScheduleTaskByCondition(condition);
        scheduleTask.setTime(time);
        scheduleTask.setCronExpression(TimeUtil.ParseTimeMapToSimpleCronExpression(TimeUtil.parseTimeString(time)));
        User user= (User) request.getSession().getAttribute("user");
        SensitiveOperation sensitiveOperation=new SensitiveOperation();
        sensitiveOperation.setAction("修改定时任务：任务名："+scheduleTaskName+" 时间："+time);
        sensitiveOperation.setTime(TimeUtil.getNowTime());
        sensitiveOperation.setOperator(user.getUsername());
        logService.insertSensitiveOperationLog(sensitiveOperation);
        JSONObject json=new JSONObject();
        json.put("message","任务修改成功！");
        WebUtil.printJSON(json.toJSONString(),response);
    }


    /**
     * 查询任务是否存在(条件查询)
     * @param taskName 任务名
     * @return
     * @throws SchedulerException
     */
    @GetMapping("/queryTask")
    @ResponseBody
    public void queryTask(String taskName,HttpServletResponse response) throws SchedulerException, IllegalAccessException {
        Map<String,String> condition=new HashedMap<>();
        condition.put("schedule_task",taskName);
        ScheduleTask scheduleTask=taskService.getScheduleTaskByCondition(condition);
        JSONObject json=new JSONObject();
        if(scheduleTask==null){
            json.put("message","任务不存在！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            Class clz=scheduleTask.getClass();
            Field[] fields=clz.getDeclaredFields();
            for(Field field:fields){
                field.setAccessible(true);
                String key=field.getName();
                if(key=="id"){
                    int value=Integer.parseInt((String) field.get(scheduleTask));
                    json.put(key,value);
                }
                String value= (String) field.get(scheduleTask);
                json.put(key,value);
            }
            json.put("message","任务不存在！");
        }
    }

    /**
     * 获取所有定时任务
     * @return
     */
    @GetMapping("/getAllScheduleTask")
    @ResponseBody
    public List getAllScheduleTask(){
         Map<String,Object> map=taskService.getAllScheduleTask();
         Map<String,String> totalPageMap= (Map<String, String>) map.get("totalPage");
         List<ScheduleTask> scheduleTasks= (List<ScheduleTask>) map.get("scheduleTasks");
         List jsonList=new ArrayList();
         jsonList.add(totalPageMap);
         jsonList.add(scheduleTasks);
         return jsonList;
    }

    @GetMapping("/getAllAcademy")
    @ResponseBody
    public List getAllAcademy(String pageNum){
        PageHelper.startPage(Integer.parseInt(pageNum),20);
        List<Academy> academies=adminDao.getAllAcademy();
        PageInfo<Academy> info=new PageInfo<>(academies);
        Map map=new HashedMap();
        map.put("totalPage",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(map);
        jsonList.add(academies);
        return jsonList;
    }

    @GetMapping("/addAcademy")
    public void addAcademy(String academyName,HttpServletResponse response,HttpServletRequest request){
        boolean isInsert=adminService.insertAcademy(academyName);
        JSONObject json=new JSONObject();
        if(isInsert){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("添加学院：学院名："+academyName);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","success");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","新增失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @GetMapping("/updateAcademy")
    public void updateAcademy(String academyId,String academyName,HttpServletResponse response,HttpServletRequest request){
        boolean isUpdate=adminService.updateAcademy(academyId, academyName);
        JSONObject json=new JSONObject();
        if(isUpdate){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("修改学院：学院id："+academyId+" 修改后名字："+academyName);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
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

    @GetMapping("/getAllMajor")
    @ResponseBody
    public List getAllMajor(int pageNum){
        PageHelper.startPage(pageNum,20);
        List<Major> majors=majorService.getAllMajor();
        PageInfo<Major> info=new PageInfo<>(majors);
        Map<String,Object> map=new HashedMap<>();
        map.put("totalPage",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(map);
        jsonList.add(majors);
        return jsonList;
    }

    @GetMapping("/searchMajor")
    @ResponseBody
    public List searchMajor(String academyName,String majorName,int pageNum){
        Map<String,String> conditionMap=new HashedMap<>();
        conditionMap.put("academyName",academyName);
        conditionMap.put("majorName",majorName);
        PageHelper.startPage(pageNum,20);
        List<Major> majors=majorService.getMajorByCondition(conditionMap);
        PageInfo<Major> info=new PageInfo<>(majors);
        Map<String,Object> infoMap=new HashedMap<>();
        infoMap.put("totoalPage",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(infoMap);
        jsonList.add(majors);
        return jsonList;
    }

    @GetMapping("/insertMajor")
    public void insertMajor(String academyName,String majorName,String xuezhi,HttpServletResponse response,HttpServletRequest request){
        Major major=new Major();
        major.setAcademyName(academyName);
        major.setMajorName(majorName);
        boolean isInsert=majorService.insertMajor(major,xuezhi);
        JSONObject json=new JSONObject();
        if(isInsert){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("添加专业：专业名："+majorName);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","添加成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","添加失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @GetMapping("/updateMajor")
    public void updateMajor(String id,String majorName,HttpServletResponse response,HttpServletRequest request){
        boolean isUpdate=majorService.updateMajor(majorName, id);
        JSONObject json=new JSONObject();
        if(isUpdate){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("修改专业：id："+id+" 修改后名字："+majorName);
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

    @GetMapping("/deleteMajor")
    public void deleteMajor(String id,HttpServletResponse response,HttpServletRequest request){
        boolean isDelete=majorService.deleteMajor(id);
        JSONObject json=new JSONObject();
        if(isDelete){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("删除专业：id："+id);
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

    @GetMapping("/queryCourseByCondition")
    @ResponseBody
    public List queryCourseByCondition(String teacherName,String majorName,String classType,String courseName,int pageNum){
        PageHelper.startPage(pageNum,20);
        List<Course> courseList=courseService.getCourseByCondition(majorName, courseName, classType, teacherName);
        PageInfo<Course> info=new PageInfo<>(courseList);
        Map<String,Object> infoMap=new HashedMap<>();
        infoMap.put("totalPage",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(infoMap);
        jsonList.add(courseList);
        return jsonList;
    }

    @PostMapping("/updateCourse")
    public void updateCourse(@RequestBody Course course,HttpServletResponse response,HttpServletRequest request){
        boolean isUpdate=courseService.updateCourse(course);
        JSONObject json=new JSONObject();
        if(isUpdate){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("修改课程：id："+course.getCourseId());
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

    @PostMapping("/insertCourse")
    public void insertCourse(@RequestBody Course course,HttpServletResponse response,HttpServletRequest request){
        boolean isInsert=courseService.insertCourse(course);
        JSONObject json=new JSONObject();
        if(isInsert){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("添加课程：课程名："+course.getCourseName()+" 所属专业："+course.getMajorName());
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","添加成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","添加失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @GetMapping("/deleteCourse")
    public void deleteCourse(String courseId,HttpServletResponse response,HttpServletRequest request){
        boolean isDelete=courseService.deleteCourse(courseId);
        JSONObject json=new JSONObject();
        if(isDelete){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("删除课程：id："+courseId);
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

    @GetMapping("/queryClassRoomByCondition")
    @ResponseBody
    public List queryClassRoomByCondition(String academyName,String classroomType,String classroomName,int pageNum){
        PageHelper.startPage(pageNum,20);
        List<Classroom> classroomList=classroomService.getClassroomByCondition(classroomName, classroomType, academyName);
        PageInfo<Classroom> info=new PageInfo<>(classroomList);
        Map<String,Object> infoMap=new HashedMap<>();
        infoMap.put("totalMap",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(infoMap);
        jsonList.add(classroomList);
        return jsonList;
    }

    @PostMapping("/insertClassRoom")
    public void insertClassRoom(@RequestBody Classroom classroom,HttpServletResponse response,HttpServletRequest request){
        boolean isInsert=classroomService.insertClassroom(classroom);
        JSONObject json=new JSONObject();
        if(isInsert){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("添加教室：教室名："+classroom.getClassroomName());
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","添加成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","添加失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @PostMapping("/updateClassRoom")
    public void updateClassRoom(@RequestBody Classroom classroom,HttpServletResponse response,HttpServletRequest request){
        boolean isUpdate=classroomService.updateClassroom(classroom);
        JSONObject json=new JSONObject();
        if(isUpdate){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("修改教室：id："+classroom.getId());
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

    @GetMapping("/deleteClassRoom")
    public void deleteClassRoom(String classroomName,HttpServletResponse response,HttpServletRequest request){
        boolean isDelete=classroomService.deleteClassroom(classroomName);
        JSONObject json=new JSONObject();
        if(isDelete){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("删除教室：教室名："+classroomName);
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

    @GetMapping("/queryMajorClassByCondition")
    @ResponseBody
    public List queryMajorClassByCondition(String majorName,String className,int pageNum){
        PageHelper.startPage(pageNum,20);
        List<MajorClass> majorClassList=majorClassService.getMajorClassByCondition(majorName, className);
        PageInfo<MajorClass> info=new PageInfo<>(majorClassList);
        Map<String,Object> infoMap=new HashedMap<>();
        infoMap.put("totalPage",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(infoMap);
        jsonList.add(majorClassList);
        return jsonList;
    }

    /**
     * 添加班级
     * @param majorName
     * @param majorClassNum
     * @param response
     * @param request
     */
    @GetMapping("/insertMajorClass")
    public void insertMajorClass(String majorName,int majorClassNum,HttpServletResponse response,HttpServletRequest request){
        boolean isInsert=majorClassService.insertMajorClass(majorName, majorClassNum);
        JSONObject json=new JSONObject();
        if(isInsert){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("添加班级：专业名："+majorName);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","添加成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","添加失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    /**
     * 删除班级
     * @param className
     * @param response
     * @param request
     */
    @GetMapping("/deleteMajorClass")
    public void deleteMajorClass(String className,HttpServletResponse response,HttpServletRequest request){
        boolean isDelete=majorClassService.deleteMajorClass(className);
        JSONObject json=new JSONObject();
        if(isDelete){
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("删除班级：班级名："+className);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("messgae","删除成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","删除失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    /**
     * 上传学生学籍信息
     * @param response
     * @param request
     * @throws IOException
     * @throws ServletException
     */
    @PostMapping("/uploadStudentStatusMsgFile")
    public void uploadStudentStatusMsgFile(HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
        List<Part> parts= (List<Part>) request.getParts();
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
        excelService.resolveExcelAndInsertStudentStatusMsg(response,fileNames,request);
    }

    /**
     * 上传新职工信息并为其注册新账号
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    @PostMapping("/uploadTeacherMsgFile")
    public void uploadTeacherMsgFile(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
        List<Part> parts= (List<Part>) request.getParts();
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
        excelService.resolveExcelAndInsertTeacherMsg(response,fileNames,request);
    }

    /**
     * 上传新生信息并为其注册新的账号
     * @param response
     * @param request
     * @throws Exception
     */
    @PostMapping("/uploadStudentMsgFile")
    public void uploadStudentMsgFile(HttpServletResponse response,HttpServletRequest request) throws Exception {
        List<Part> parts= (List<Part>) request.getParts();
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
        excelService.resolveExcelAndInsertStudentMsg(response,fileNames,request);
    }

    /**
     * 上传并解析课程表excel
     * @param response
     * @param request
     * @throws IOException
     * @throws ServletException
     */
    @PostMapping("/uploadTimestableFile")
    public void uploadTimestableFile(HttpServletResponse response,HttpServletRequest request) throws IOException, ServletException {
        List<Part> parts= (List<Part>) request.getParts();
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
        excelService.resolveExcelAndInsertTimestable(response,fileNames,request);
    }

}
