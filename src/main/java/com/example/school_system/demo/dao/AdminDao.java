package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AdminDao {
    public int updatePasswordByUsername(@Param("info") Map<String,String> info);
    public List<Role> selectUserRoleByCondition(@Param("conditionMap") Map<String,String> conditionMap);
    public int updateUserRoleByUsername(Role role);
    public List<PreSelectCourseTask> getAllMajorClassCourse();
    public int insertBatchPreSelectCourseTask(List<PreSelectCourseTask> preSelectCourseTasks);
    public List<PreSelectCourseTask> getTaskByCondition(@Param("map") Map<String,String> conditionMap);
    public int insertTaskForCustomMode(List<PreSelectCourseTask> preSelectCourseTasks);
    public List<PreSelectCourseTask> getMajorClassCourseByCondition(@Param("map")Map<String,String> conditionMap);
    public int deleteTaskByClassName(String className);
    public int deleteAllSuperModeTask();
    public String getLastAdminUsername();
    public int insertAdminAccount(User user);
    public List<SensitiveOperation> getAllLog();
    public List<Academy> getAllAcademy();
    public int insertAcademy(String academyName);
    public int updateAcademy(@Param("academyId") String academyId,@Param("academyName") String academyName);
}
