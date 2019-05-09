package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.StudentPersonalMessageDao;
import com.example.school_system.demo.pojo.Academy;
import com.example.school_system.demo.pojo.Major;
import com.example.school_system.demo.pojo.Student;
import com.example.school_system.demo.service.AcademyService;
import com.example.school_system.demo.service.MajorService;
import com.example.school_system.demo.service.StudentPersonalMsgService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentPersonalMsgServiceImpl implements StudentPersonalMsgService {

    @Autowired
    private StudentPersonalMessageDao studentPersonalMessageDao;
    @Autowired
    private AcademyService academyService;
    @Autowired
    private MajorService majorService;

    @Override
    public boolean insertBatchStudentPersonalMessage(List<Student> studentList) {
        int result=studentPersonalMessageDao.insertBatchStudentPersonalMessage(studentList);
        if(result>0){
            return true;
        }
        return false;
    }

    @Override
    public String getLastStudentIdByClassId(String classId) {
        return studentPersonalMessageDao.getLastStudentIdByClassId(classId);
    }

    @Override
    public List<Academy> countAcademyPeopleNum() {
        List<String> allAcademyName=academyService.getAllAcademyName();
        List<Academy> allAcademy=new ArrayList<>();
        allAcademyName.forEach(academyName->{
            Academy academy=new Academy();
            academy.setAcademyName(academyName);
            academy.setPeopleNum(studentPersonalMessageDao.countAcademyPeopleNum(academyName));
            allAcademy.add(academy);
        });
        return allAcademy;
    }

    @Override
    public List<Major> countMajorPeopleNum() {
        List<Major> allMajor=majorService.getAllMajor();
        List<String> allMajorName=new ArrayList<>();
        allMajor.forEach(major -> {
            allMajorName.add(major.getMajorName());
        });
        List<Major> majors=new ArrayList<>();
        allMajorName.forEach(majorName->{
            Major major=new Major();
            major.setMajorName(majorName);
            major.setPeopleNum(String.valueOf(studentPersonalMessageDao.countMajorPeopleNum(majorName)));
            majors.add(major);
        });
        return majors;
    }

}
