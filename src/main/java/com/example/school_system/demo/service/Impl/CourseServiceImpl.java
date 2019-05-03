package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.CourseDao;
import com.example.school_system.demo.dao.MajorDao;
import com.example.school_system.demo.pojo.Course;
import com.example.school_system.demo.pojo.Major;
import com.example.school_system.demo.service.CourseService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseDao courseDao;
    @Autowired
    private MajorDao majorDao;

    @Override
    public List<Course> getCourseByCondition(String majorName, String courseName, String classType, String teacherName) {
        Map<String,String> conditionMap=new HashedMap<>();
        conditionMap.put("majorName",majorName);
        conditionMap.put("courseName",courseName);
        conditionMap.put("classType",classType);
        conditionMap.put("teacherName",teacherName);
        return courseDao.getCourseByCondition(conditionMap);
    }

    @Override
    public boolean insertCourse(Course course) {
        Map<String,String> idsMap=getNewCourseId(course);
        course.setId(idsMap.get("newId"));
        course.setCourseId(idsMap.get("newCourseId"));
        int result=courseDao.insertCourse(course);
        if(result>0){
            return true;
        }
        return false;
    }

    @Override
    public boolean updateCourse(Course course) {
        int result=courseDao.updateCourse(course);
        if(result>0){
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteCourse(String id) {
        int result=courseDao.deleteCourse(id);
        if(result>0){
            return true;
        }
        return false;
    }

    /**
     * 返回包含新的courseId和id的map
     * id=学院id+专业Id+课程id
     * @param course
     * @return
     */
    @Override
    public Map<String,String> getNewCourseId(Course course) {
        Map<String,String> conditionMap=new HashedMap<>();
        conditionMap.put("majorName",course.getMajorName());
        List<Major> majors=majorDao.getMajorByCondition(conditionMap);
        Major major=majors.get(0);
        String majorId=major.getMajorId();
        String academyId=major.getId().substring(0,2);
        String lastCourseId=courseDao.getLastCourseIdByMajorName(course.getMajorName());
        String newLastCourseId=String.valueOf(Integer.parseInt(lastCourseId)+1);
        if(newLastCourseId.length()<3){
            String tempStr="0";
            for(int temp=newLastCourseId.length();temp<2;temp++){
                tempStr=tempStr+tempStr;
            }
            newLastCourseId=tempStr+newLastCourseId;
        }
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append(academyId).append(majorId).append(newLastCourseId);
        Map<String,String> idsMap=new HashedMap<>();
        idsMap.put("newCourseId",newLastCourseId);
        idsMap.put("newId",stringBuffer.toString());
        return idsMap;
    }
}
