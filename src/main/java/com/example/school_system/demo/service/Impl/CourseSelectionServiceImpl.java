package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.CourseSelectionDao;
import com.example.school_system.demo.dao.StudentDao;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.AdminService;
import com.example.school_system.demo.service.CourseSelectionService;
import com.example.school_system.demo.service.StudentService;
import com.example.school_system.demo.service.TaskService;
import com.example.school_system.demo.utils.StringUtil;
import com.example.school_system.demo.utils.TimeUtil;
import com.example.school_system.demo.utils.WebUtil;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class CourseSelectionServiceImpl implements CourseSelectionService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AdminService adminService;
    @Autowired
    private CourseSelectionDao courseSelectionDao;
    @Autowired
    private TaskService taskService;

    private static final String HASH_PREFIX="course:";
    private static final String COURSE_SET_PREFIX="course:select:";
    private static final String STUDENT_SET_PREFIX="student:";
    final public static String PRE_SELECT_COURSE_JOB_NAME_FOR_SUPER_MODE="afterSelectCourse";
    final public static String PRE_SELECT_COURSE_JOB_NAME_FOR_CUSTOM_MODE="afterSelectCourseforCustomMode";
    final public static String PRE_SELECT_COURSE_JOB_CLASS_NAME="com.example.school_system.demo.scheduleJob.AfterSelectCourse";
    final public static String JOB_GROUP_NAME="group1";

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertCourseSelection(String courseId, String studentId, String peopleNum) {
        courseSelectionDao.insertCourseSelection(courseId, studentId, peopleNum);
    }

    @Override
    public void setCourseInRedis(Object data) {
        List<CourseVo> courseVos= (List<CourseVo>) data;
        for(int j=0;j<courseVos.size();j++){
            String id=courseVos.get(j).getId();
            redisTemplate.opsForHash().put("course:"+id,"id",id);
            redisTemplate.opsForHash().put("course:"+id,"courseName",courseVos.get(j).getCourseName());
            redisTemplate.opsForHash().put("course:"+id,"teacherName",courseVos.get(j).getTeacherName());
        }
    }

    /**
     * 注意：在redis中使用lua脚本和事务必须小心！若是在脚本在编译过程中出错则不会运行；但若是在编译后执行过程中出错，redis是不会回滚事务的，所以需要谨慎使用！
     * @param courseId 课程id
     * @param studentId 学生id
     * @return  -1：已经选过此课程 0：课程可选人数不足 1：选课成功
     */
    @Override
    public Long selectCourse(String courseId, String studentId) {
        String sha1=null;
        //流程：先判断所选择的课程是否被选完了，然后判断是否已经选了此课程，若都不符合前面两个条件，则扣除课程可选人数并写入选课记录
        String script=
                "local peopleNum=tonumber(redis.call('hget','KEYS[1]','peopleNum')) \n"
                        +"if peopleNum == 0 then return 0 end \n"
                        +"local isSelect=redis.call('sismember',KEYS[2],ARGV[1]) \n"
                        +"if isSelect ~= 0 then return -1 end \n"
                        +"redis.call('hincrby',KEYS[1],'peopleNum',-1) \n"
                        +"redis.call('sadd',KEYS[2],ARGV[1]) \n"
                        +"redis.call('sadd',KEYS[3],ARGV[2])  \n"
                        +"return 1 \n";
        Jedis jedis= (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
        if(sha1==null){
            //若此脚本是第一次加载，则先将脚本加载到redis中，返回sha1,以方便下次使用
            sha1=jedis.scriptLoad(script);
        }
        Long result= (Long) jedis.evalsha(sha1,3,HASH_PREFIX+courseId,COURSE_SET_PREFIX+courseId,STUDENT_SET_PREFIX+studentId,studentId,courseId);
        return result;
    }

    /**
     * 把选择某个课程的所有学生的id组合成json，存放在数据库中
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void putCourseSelectionToDatabase() {
        List<Course> courses=courseSelectionDao.defaultGetCourse();
        for(int i=0;i<courses.size();i++){
            String courseId=courses.get(i).getId();
            Set<String> dataFromRedis=redisTemplate.opsForSet().members(COURSE_SET_PREFIX+courseId);
            JSONObject json=new JSONObject();
            dataFromRedis.forEach(value->{
                String student=value;
                for(int j=0;j<dataFromRedis.size();j++){
                    if(student!=null){
                        json.put("student"+j,student);
                    }
                }
            });
            String studentId=json.toJSONString();
            String peopleNum=Integer.toString(json.size());
            courseSelectionDao.insertCourseSelection(courseId,studentId,peopleNum);
        }
    }

    @Override
    public Long cancelSelectedCourse(String courseId,String studentId) {
        String sha1=null;
        String script="local isSelect=redis.call('sismember',KEYS[1],ARGV[1]) \n"
                +"if isSelect ~= 0 then \n"
                +"redis.call('srem',KEYS[1],ARGV[1]) \n"
                +"redis.call('srem',KEYS[2],ARGV[2]) \n"
                +"redis.call('hincrby',KEYS[3],'peopleNum',1) end \n"
                +"return 1 \n";
        Jedis jedis= (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
        if(sha1==null){
            sha1=jedis.scriptLoad(script);
        }
        Long result= (Long) jedis.evalsha(sha1,3,COURSE_SET_PREFIX+courseId,STUDENT_SET_PREFIX+studentId,HASH_PREFIX+courseId,studentId,courseId);
        return result;
    }

    @Override
    public List<SelectCourseResult> getSelectCourseResult(String majorName) {
        return courseSelectionDao.getSelectCourseResult(majorName);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void uploadTaskForSuperMode(String startTime, String endTime, HttpServletResponse response) {
        String mode="一键开启模式";
        List<PreSelectCourseTask> preSelectCourseTasks=adminService.getAllMajorClassCourse();
        boolean isInsert=adminService.insertBatchPreSelectCourseTask(preSelectCourseTasks,startTime,endTime,mode);
        JSONObject json=new JSONObject();
        if(isInsert){
            String jobName=PRE_SELECT_COURSE_JOB_NAME_FOR_SUPER_MODE;
            String scheduleTaskName="预选信息导入数据库";
            int state= 0;
            try {
                state = setScheduleTaskForPreSelectCourse(endTime,scheduleTaskName,jobName);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
     * 为预选任务新加数据导入定时任务
     * @param time 定时任务开始时间
     * @param scheduleTaskName 定时任务名字（数据库）
     * @param jobName 定时任务名字（系统）
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(propagation = Propagation.NESTED)
    public int setScheduleTaskForPreSelectCourse(String time, String scheduleTaskName, String jobName) throws Exception {
        Map<String,String> conditionMap=new HashMap<>();
        conditionMap.put("schedule_task",scheduleTaskName);
        conditionMap.put("time",time);
        ScheduleTask scheduleTask=taskService.getScheduleTaskByCondition(conditionMap);
        //判断定时任务是否已经存在数据库中和系统定时任务
        if(scheduleTask==null){
            ScheduleTask task=new ScheduleTask();
            task.setScheduleTask(scheduleTaskName);
            Map<String,String> timeMap= TimeUtil.parseTimeString(time);
            //判断预选任务结束时间是否在02:：00后
            if(Integer.parseInt(timeMap.get("hour"))>2){
                String taskDay=String.valueOf(Integer.parseInt(timeMap.get("day"))+1);
                String taskStartTime=timeMap.get("year")+"-"+timeMap.get("month")+"-"+taskDay+" "+"02:00:00";
                boolean timeIsIllegal=TimeUtil.checkMonthAndDayIslegal(timeMap.get("year"),timeMap.get("month"),taskDay);
                if(!timeIsIllegal){
                    taskDay="01";
                    timeMap.put("month",String.valueOf(Integer.parseInt(timeMap.get("month"))+1));
                    taskStartTime=timeMap.get("year")+"-"+timeMap.get("month")+"-"+taskDay+" "+"02:00:00";
                }
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

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void uploadTaskForCustomMode(String majorId, String classId, String startTime, String endTime, HttpServletResponse response) throws Exception {
        String mode="自定义模式";
        Map<String,String> conditionMap=new HashMap<>();
        if(classId==""){
            conditionMap.put("major_name",majorId+"%");
        }else{
            conditionMap.put("class_name",classId);
        }
        List<PreSelectCourseTask> preSelectCourseTasks=adminService.getMajorClassCourseByCondition(conditionMap);
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
            boolean isUpload=adminService.insertTaskForCustomMode(preSelectCourseTasks,startTime,endTime,mode);
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
}
