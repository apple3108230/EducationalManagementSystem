package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.AdminDao;
import com.example.school_system.demo.dao.MajorDao;
import com.example.school_system.demo.pojo.Major;
import com.example.school_system.demo.service.MajorService;
import com.example.school_system.demo.service.StudentPersonalMsgService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class MajorServiceImpl implements MajorService {

    @Autowired
    private MajorDao majorDao;
    @Autowired
    private StudentPersonalMsgService studentPersonalMsgService;

    @Override
    public List<Major> getAllMajor() {
        return majorDao.getAllMajor();
    }

    @Override
    public List<Major> getMajorByCondition(Map<String, String> conditionMap) {
        return majorDao.getMajorByCondition(conditionMap);
    }

    @Override
    public Map<String, String> getLastIdAndMajorIdByAcademy(String academyName) {
        return majorDao.getLastIdAndMajorIdByAcademy(academyName);
    }

    @Override
    public boolean insertMajor(Major major,String xuezhi) {
        Map<String,String> idsMap=getLastIdAndMajorIdByAcademy(major.getAcademyName());
        idsMap.put("xuezhi",xuezhi);
        Map<String,String> newIdsMap=getNewIdAndMajorId(idsMap);
        major.setId(newIdsMap.get("newId"));
        major.setMajorId(newIdsMap.get("newMajorId"));
        int result=majorDao.insertMajor(major);
        if (result > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean updateMajor(String majorName,String id) {
        int result=majorDao.updateMajor(majorName,id);
        if(result>0){
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteMajor(String majorId) {
        int result=majorDao.deleteMajor(majorId);
        if(result>0){
            return true;
        }
        return false;
    }

    /**
     * id格式：所属学院编号+专业编号+学制
     * @param info
     * @return
     */
    @Override
    public Map<String,String> getNewIdAndMajorId(Map<String,String> info){
        String academyId=info.get("academyId");
        String majorId=info.get("majorId");
        String xuezhi=info.get("xuezhi");
        String newMajorId=String.valueOf(Integer.parseInt(majorId)+1);
        if(newMajorId.length()==1){
            newMajorId="0"+newMajorId;
        }
        StringBuffer newId=new StringBuffer();
        newId.append(academyId).append(newMajorId).append(xuezhi);
        Map<String,String> newIdsMap=new HashedMap<>();
        newIdsMap.put("newId",newId.toString());
        newIdsMap.put("newMajorId",newMajorId);
        return newIdsMap;
    }

    @Transactional(propagation = Propagation.NESTED)
    @Override
    public boolean updateMajorPeopleNum(List<Major> majors) {
        int result=0;
        for(Major major:majors){
            result=result+majorDao.updateMajorPeopleNum(major);
        }
        if(result==majors.size()){
            return true;
        }
        return false;
    }

}
