package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.MajorClassDao;
import com.example.school_system.demo.dao.MajorDao;
import com.example.school_system.demo.pojo.Major;
import com.example.school_system.demo.pojo.MajorClass;
import com.example.school_system.demo.pojo.enums.MajorClassNumEnum;
import com.example.school_system.demo.service.MajorClassService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MajorClassServiceImpl implements MajorClassService {

    @Autowired
    private MajorClassDao majorClassDao;
    @Autowired
    private MajorDao majorDao;

    @Override
    public List<MajorClass> getMajorClassByCondition(String majorName, String className) {
        Map<String,String> conditionMap=new HashMap<>();
        conditionMap.put("majorName",majorName);
        conditionMap.put("className",className);
        return majorClassDao.getMajorClassByCondition(conditionMap);
    }

    @Override
    public boolean insertMajorClass(String majorName,int majorClassNum) {
        Map<String,String> conditionMap=new HashedMap<>();
        conditionMap.put("majorName",majorName);
        Major major=majorDao.getMajorByCondition(conditionMap).get(0);
        String majorId=major.getMajorId();
        String xuezhi=String.valueOf(majorId.charAt(3));
        String peopleNum=major.getPeopleNum();
        int classNum=Integer.valueOf(peopleNum)/majorClassNum;
        int remainder=Integer.valueOf(peopleNum)%majorClassNum;
        List<MajorClass> majorClassList=new ArrayList<>();
        for(int i=1;i<majorClassNum+1;i++){
            String classId= MajorClassNumEnum.getChineseNum(i);
            StringBuffer newMajorClassId=new StringBuffer();
            newMajorClassId.append(majorId).append(i);
            MajorClass majorClass=new MajorClass();
            majorClass.setId(newMajorClassId.toString());
            majorClass.setClassId(String.valueOf(i));
            majorClass.setClassName(majorName+classId);
            majorClass.setMajorName(majorName);
            majorClass.setXuezhi(xuezhi);
            if(i==majorClassNum){
                majorClass.setPeopleNum(String.valueOf(classNum+remainder));
            }else{
                majorClass.setPeopleNum(String.valueOf(classNum));
            }
            majorClassList.add(majorClass);
        }
        int result=majorClassDao.insertMajorClass(majorClassList);
        if(result>0){
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteMajorClass(String className) {
        int result=majorClassDao.deleteMajorClass(className);
        if(result>0){
            return true;
        }
        return false;
    }
}
