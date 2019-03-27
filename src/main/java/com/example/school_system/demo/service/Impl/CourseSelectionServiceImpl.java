package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.CourseSelectionDao;
import com.example.school_system.demo.dao.StudentDao;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.CourseSelectionService;
import com.example.school_system.demo.service.StudentService;
import com.example.school_system.demo.utils.StringUtil;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CourseSelectionServiceImpl implements CourseSelectionService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CourseSelectionDao courseSelectionDao;

    private static final String HASH_PREFIX="course:";
    private static final String COURSE_SET_PREFIX="course:select:";
    private static final String STUDENT_SET_PREFIX="student:";

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
}