package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.AdminDao;
import com.example.school_system.demo.pojo.PreSelectCourseTask;
import com.example.school_system.demo.pojo.Role;
import com.example.school_system.demo.pojo.ScheduleTask;
import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.service.AdminService;
import com.example.school_system.demo.utils.StringUtil;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminDao adminDao;

    final private static String DEFAULT_ADMIN_PASSWORD="admin123";

    @Override
    public boolean updateUserRoleByUsername(Role role) {
        int result=adminDao.updateUserRoleByUsername(role);
        if (result >= 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean insertBatchPreSelectCourseTask(List<PreSelectCourseTask> preSelectCourseTasks, String startTime, String endTime) {
        preSelectCourseTasks.forEach(value->{
            value.setTime(startTime+"~"+endTime);
            value.setId(StringUtil.CustomUUID());
        });
        int result=adminDao.insertBatchPreSelectCourseTask(preSelectCourseTasks);
        if(result>=0){
            return true;
        }
        return false;
    }

    @Override
    public List<PreSelectCourseTask> getTaskByCondition(String academyNameCondition, String majorNameCondition, String classIdCondition) {
        Map<String,String> conditionMap=new HashMap<>();
        conditionMap.put("academyNameCondition",academyNameCondition);
        conditionMap.put("majorNameCondition",majorNameCondition);
        conditionMap.put("classIdCondition",classIdCondition);
        List<PreSelectCourseTask> preSelectCourseTasks=adminDao.getTaskByCondition(conditionMap);
        return preSelectCourseTasks;
    }

    @Override
    public boolean insertTaskForCustomMode(List<PreSelectCourseTask> preSelectCourseTasks, String startTime, String endTime) {
        preSelectCourseTasks.forEach(value->{
            value.setTime(startTime+"~"+endTime);
            value.setId(StringUtil.CustomUUID());
        });
        int result=adminDao.insertTaskForCustomMode(preSelectCourseTasks);
        if(result>=0){
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteTaskByClassName(String className) {
        int result=adminDao.deleteTaskByClassName(className);
        if(result>=0){
            return true;
        }
        return false;
    }

    @Override
    public User creditAdminAccount() {
        String username=adminDao.getLastAdminUsername();
        String nextUsernameNum=String.valueOf(Integer.parseInt(username)+1);
        int size=nextUsernameNum.length();
        String nextUserName="0"+nextUsernameNum;
        for(int i=size;i<5;i++){
            nextUserName="0"+nextUserName;
        }
        ByteSource source=ByteSource.Util.bytes(nextUserName);
        String salt=source.toString();
        String password=new SimpleHash("MD5",DEFAULT_ADMIN_PASSWORD,source,1024).toString();
        User user=new User(nextUserName,password);
        user.setSalt(salt);
        user.setRole("admin");
        user.setPermission("admin");
        user.setEmail("");
        int result=adminDao.insertAdminAccount(user);
        if(result>=0){
            return user;
        }
        return null;
    }

}
