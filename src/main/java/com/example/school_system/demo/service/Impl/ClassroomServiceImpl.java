package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.ClassroomDao;
import com.example.school_system.demo.pojo.Classroom;
import com.example.school_system.demo.pojo.enums.ClassRoomTypeEnum;
import com.example.school_system.demo.service.ClassroomService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ClassroomServiceImpl implements ClassroomService {

    @Autowired
    private ClassroomDao classroomDao;

    @Override
    public List<Classroom> getClassroomByCondition(String classroomName, String classroomType, String academyName) {
        Map<String,String> conditionMap=new HashedMap<>();
        conditionMap.put("classroomName",classroomName);
        conditionMap.put("classroomType",classroomType);
        conditionMap.put("academyName",academyName);
        return classroomDao.getClassroomByCondition(conditionMap);
    }

    @Override
    public boolean insertClassroom(Classroom classroom) {
        String classroomType=classroom.getClassroomType();
        classroom.setPeopleNum(ClassRoomTypeEnum.getPeopleNum(classroomType));
        int result=classroomDao.insertClassroom(classroom);
        if(result>0){
            return true;
        }
        return false;
    }

    @Override
    public boolean updateClassroom(Classroom classroom) {
        String classroomType=classroom.getClassroomType();
        classroom.setPeopleNum(ClassRoomTypeEnum.getPeopleNum(classroomType));
        int result=classroomDao.updateClassroom(classroom);
        if(result>0){
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteClassroom(String classroomName) {
        int result=classroomDao.deleteClassroom(classroomName);
        if(result>0){
            return true;
        }
        return false;
    }
}
