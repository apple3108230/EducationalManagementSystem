package com.example.school_system.demo.controller;

import com.example.school_system.demo.dao.AdminDao;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.AdminService;
import com.example.school_system.demo.service.BaseService;
import com.example.school_system.demo.service.Impl.TaskServiceImpl;
import com.example.school_system.demo.service.TaskService;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.sql.SQLException;
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

    final private static String ORIGINAL_PASSWORD="123456";
    final private static String PRE_SELECT_COURSE_JOB_NAME_FOR_SUPER_MODE="afterSelectCourse";
    final private static String PRE_SELECT_COURSE_JOB_NAME_FOR_CUSTOM_MODE="afterSelectCourseforCustomMode";
    final private static String PRE_SELECT_COURSE_JOB_CLASS_NAME="com.example.school_system.demo.scheduleJob.AfterSelectCourse";
    final private static String JOB_GROUP_NAME="group1";

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
    public void resetUser(String username,@RequestParam(required = false) String checkboxValue,HttpServletResponse response){
        JSONObject json=new JSONObject();
        User user=baseService.getUserByUserName(username);
        if(user==null){
            json.put("message","重置失败！原因（无此用户！）");
            WebUtil.printJSON(json.toJSONString(),response);
        }
        ByteSource source=ByteSource.Util.bytes(username);
        String salt=source.toString();
        int hashInterations=1024;
        String password=(new SimpleHash("MD5",ORIGINAL_PASSWORD,source,hashInterations)).toString();
        Map<String,String> info=new HashMap<>();
        info.put("username",username);
        info.put("salt",salt);
        info.put("password",password);
        if(checkboxValue!=null){
            info.put("resetBindEmail","true");
        }
        int result=adminDao.updatePasswordByUsername(info);
        if(result>=0){
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
    public void creditAdminAccount(HttpServletResponse response){
        User user=adminService.creditAdminAccount();
        JSONObject json=new JSONObject();
        if(user!=null){
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
    public void updateRole(@RequestBody Role role,HttpServletResponse response){
        role.getRole().replace(" ",",");
        boolean isUpdate=adminService.updateUserRoleByUsername(role);
        JSONObject json=new JSONObject();
        if (isUpdate) {
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
    public void uploadTaskForSuperMode(String startTime,String endTime,HttpServletResponse response) throws Exception {
        List<PreSelectCourseTask> preSelectCourseTasks=adminDao.getAllMajorClassCourse();
        boolean isInsert=adminService.insertBatchPreSelectCourseTask(preSelectCourseTasks,startTime,endTime);
        JSONObject json=new JSONObject();
        if(isInsert){
            String jobName=PRE_SELECT_COURSE_JOB_NAME_FOR_SUPER_MODE;
            String scheduleTaskName="预选信息导入数据库";
            int state=setScheduleTaskForPreSelectCourse(endTime,scheduleTaskName,jobName);
            if(state==TaskServiceImpl.SET_SCHEDULE_TASK_INTO_DATABASE_IS_SUCCESS){
                json.put("message","success");
                WebUtil.printJSON(json.toJSONString(),response);
            }else if(state==TaskServiceImpl.SCHEDULE_TASK_IS_EXIST){
                json.put("message","设置定时任务失败！原因：SCHEDULE_TASK_IS_EXIST");
                WebUtil.printJSON(json.toJSONString(),response);
            }else if(state==TaskServiceImpl.FAIL_TO_SET_SCHEDULE_TASK_INTO_DATABASE){
                json.put("message","设置定时任务失败！原因：FAIL_TO_SET_SCHEDULE_TASK_INTO_DATABASE");
                WebUtil.printJSON(json.toJSONString(),response);
            }
        }else{
            json.put("message","添加预选课任务失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
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
    public void uploadTaskForCustomMode(String majorId,String classId,String startTime,String endTime,HttpServletResponse response) throws Exception {
        Map<String,String> conditionMap=new HashMap<>();
        if(classId==""){
            conditionMap.put("major_name",majorId+"%");
        }else{
            conditionMap.put("class_name",classId);
        }
        List<PreSelectCourseTask> preSelectCourseTasks=adminDao.getMajorClassCourseByCondition(conditionMap);
        JSONObject json=new JSONObject();
        if(preSelectCourseTasks.size()==0){
            if(majorId==null){
                json.put("message","您输入的班级编号不存在！");
                WebUtil.printJSON(json.toJSONString(),response);
            }else{
                json.put("message","您输入的专业编号不存在！");
                WebUtil.printJSON(json.toJSONString(),response);
            }
        }else{
            boolean isUpload=adminService.insertTaskForCustomMode(preSelectCourseTasks,startTime,endTime);
            if(isUpload){
                String jobName=PRE_SELECT_COURSE_JOB_NAME_FOR_CUSTOM_MODE;
                String scheduleTaskName="预选信息导入数据库(自定义)";
                int state=setScheduleTaskForPreSelectCourse(endTime,scheduleTaskName,jobName);
                if(state==TaskServiceImpl.SET_SCHEDULE_TASK_INTO_DATABASE_IS_SUCCESS){
                    json.put("message","success");
                    WebUtil.printJSON(json.toJSONString(),response);
                }else if(state==TaskServiceImpl.SCHEDULE_TASK_IS_EXIST){
                    json.put("message","设置定时任务失败！原因：SCHEDULE_TASK_IS_EXIST");
                    WebUtil.printJSON(json.toJSONString(),response);
                }else if(state==TaskServiceImpl.FAIL_TO_SET_SCHEDULE_TASK_INTO_DATABASE){
                    json.put("message","设置定时任务失败！原因：FAIL_TO_SET_SCHEDULE_TASK_INTO_DATABASE");
                    WebUtil.printJSON(json.toJSONString(),response);
                }
            }else{
                json.put("message","system error");
                WebUtil.printJSON(json.toJSONString(),response);
            }
        }

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
    public boolean checkAfterSelectedCourseTaskExist() throws SchedulerException {
        return taskService.checkTaskExist(PRE_SELECT_COURSE_JOB_NAME_FOR_SUPER_MODE,JOB_GROUP_NAME);
    }

    /**
     * 删除预选任务
     * @param className
     * @param response
     */
    @GetMapping("/deleteTask")
    public void deleteTask(String className,HttpServletResponse response){
        JSONObject json=new JSONObject();
        if(adminService.deleteTaskByClassName(className)){
            json.put("message","删除成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","删除失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    /**
     * 获取敏感操作日志
     * @return
     */
    @GetMapping("/getAllLog")
    @ResponseBody
    public List getAllLog(@RequestParam(value = "pageNum",required = false) String pageNum){
        if(pageNum!=""){
            PageHelper.startPage(Integer.parseInt(pageNum),20);
        }
        PageHelper.startPage(1,20);
        List<SensitiveOperation> sensitiveOperations=adminDao.getAllLog();
        PageInfo info=new PageInfo<>(sensitiveOperations);
        Map map=new HashMap();
        map.put("totalPage",info.getPages());
        List jsonList=new ArrayList();
        jsonList.add(map);
        jsonList.add(sensitiveOperations);
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
    public void startScheduleJob(HttpServletResponse response,String time,String jobName,String scheduleTaskName,String className,@RequestParam(value = "cronExpression",required = false) String cronExpression) throws SchedulerException, SQLException, ClassNotFoundException {
        ScheduleTask scheduleTask=new ScheduleTask();
        if(cronExpression!=""&&time==""){
            scheduleTask.setCronExpression(cronExpression);
        }
        scheduleTask.setTime(time);
        scheduleTask.setCronExpression(TimeUtil.ParseTimeMapToSimpleCronExpression(TimeUtil.parseTimeString(time)));
        scheduleTask.setScheduleTask(scheduleTaskName);
        scheduleTask.setJobName(jobName);
        scheduleTask.setJobGroup(JOB_GROUP_NAME);
        scheduleTask.setClassName(className);
        taskService.addTask(scheduleTask);
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
    public void deleteScheduleJob(HttpServletResponse response,String scheduleTaskName,@RequestParam(value = "time",required = false) String time,String cronExpression) throws SchedulerException, SQLException {
        Map<String,String> condition=new HashedMap<>();
        condition.put("schedule_task",scheduleTaskName);
        if(time.isEmpty()){
            condition.put("cron_expression",cronExpression);
        }else{
            condition.put("time",time);
        }
        ScheduleTask scheduleTask=taskService.getScheduleTaskByCondition(condition);
        taskService.deleteTask(scheduleTask);
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
    public void editScheduleJob(HttpServletResponse response,String scheduleTaskName,String time) throws SchedulerException {
        Map<String,String> condition=new HashedMap<>();
        condition.put("schedule_task",scheduleTaskName);
        ScheduleTask scheduleTask=taskService.getScheduleTaskByCondition(condition);
        scheduleTask.setTime(time);
        scheduleTask.setCronExpression(TimeUtil.ParseTimeMapToSimpleCronExpression(TimeUtil.parseTimeString(time)));
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

    /**
     * 为预选任务新加数据导入定时任务
     * @param time 定时任务开始时间
     * @param scheduleTaskName 定时任务名字（数据库）
     * @param jobName 定时任务名字（系统）
     * @return
     * @throws Exception
     */
    public int setScheduleTaskForPreSelectCourse(String time,String scheduleTaskName,String jobName) throws Exception {
        Map<String,String> conditionMap=new HashMap<>();
        conditionMap.put("schedule_task",scheduleTaskName);
        conditionMap.put("time",time);
        ScheduleTask scheduleTask=taskService.getScheduleTaskByCondition(conditionMap);
        //判断定时任务是否已经存在数据库中和系统定时任务
        if(scheduleTask==null){
            ScheduleTask task=new ScheduleTask();
            task.setScheduleTask(scheduleTaskName);
            Map<String,String> timeMap=TimeUtil.parseTimeString(time);
            //判断预选任务结束时间是否在02:：00后
            if(Integer.parseInt(timeMap.get("hour"))>2){
                String taskDay=String.valueOf(Integer.parseInt(timeMap.get("day"))+1);
                String taskStartTime=timeMap.get("year")+"-"+timeMap.get("month")+"-"+taskDay+" "+"02:00:00";
                task.setTime(taskStartTime);
                task.setCronExpression(TimeUtil.ParseTimeMapToSimpleCronExpression(TimeUtil.parseTimeString(taskStartTime)));
                task.setClassName(PRE_SELECT_COURSE_JOB_CLASS_NAME);
                task.setScheduleTask(scheduleTaskName);
                task.setJobName(jobName);
                task.setJobGroup(JOB_GROUP_NAME);
                return taskService.addTask(task);
            }else {
                Map<String,String> time1=TimeUtil.parseTimeString(time);
                String taskTime=time1.get("year")+"-"+time1.get("month")+"-"+time1.get("day")+" "+"02:00:00";
                task.setTime(taskTime);
                task.setCronExpression(TimeUtil.ParseTimeMapToSimpleCronExpression(TimeUtil.parseTimeString(taskTime)));
                task.setClassName(PRE_SELECT_COURSE_JOB_CLASS_NAME);
                task.setScheduleTask(scheduleTaskName);
                task.setJobName(jobName);
                task.setJobGroup(JOB_GROUP_NAME);
                return taskService.addTask(task);
            }
        }
        //若是自定义定时任务
        if(scheduleTask!=null&&taskService.checkTaskExist(scheduleTask.getJobName(),scheduleTask.getJobGroup())&&scheduleTask.getJobName().matches(PRE_SELECT_COURSE_JOB_NAME_FOR_CUSTOM_MODE)){
            String lastNum=scheduleTask.getJobName().split(PRE_SELECT_COURSE_JOB_NAME_FOR_CUSTOM_MODE)[1];
            String newTaskName;
            if(lastNum==""){
                newTaskName=scheduleTask.getJobName()+"1";
            }else{
                newTaskName=scheduleTask.getClassName()+String.valueOf(Integer.valueOf(lastNum+1));
            }
            ScheduleTask task=new ScheduleTask();
            task.setScheduleTask(scheduleTaskName);
            Map<String,String> timeMap=TimeUtil.parseTimeString(time);
            //判断预选任务结束时间是否在02:：00后
            if(Integer.parseInt(timeMap.get("hour"))>2){
                String taskDay=String.valueOf(Integer.parseInt(timeMap.get("day"))+1);
                String taskStartTime=timeMap.get("year")+"-"+timeMap.get("month")+"-"+taskDay+" "+"02:00:00";
                task.setTime(taskStartTime);
                task.setCronExpression(TimeUtil.ParseTimeMapToSimpleCronExpression(TimeUtil.parseTimeString(taskStartTime)));
                task.setClassName(PRE_SELECT_COURSE_JOB_CLASS_NAME);
                task.setScheduleTask(scheduleTaskName);
                task.setJobName(newTaskName);
                task.setJobGroup(JOB_GROUP_NAME);
                return taskService.addTask(task);
            }else {
                Map<String,String> time1=TimeUtil.parseTimeString(time);
                String taskTime=time1.get("year")+"-"+time1.get("month")+"-"+time1.get("day")+" "+"02:00:00";
                task.setTime(taskTime);
                task.setCronExpression(TimeUtil.ParseTimeMapToSimpleCronExpression(TimeUtil.parseTimeString(taskTime)));
                task.setClassName(PRE_SELECT_COURSE_JOB_CLASS_NAME);
                task.setScheduleTask(scheduleTaskName);
                task.setJobName(newTaskName);
                task.setJobGroup(JOB_GROUP_NAME);
                return taskService.addTask(task);
            }
        }
        //以上情况都不符合则直接抛出异常
        throw new Exception("unknown error");
    }
}
