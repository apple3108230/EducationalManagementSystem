package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.AcademyDao;
import com.example.school_system.demo.pojo.Academy;
import com.example.school_system.demo.service.AcademyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AcademyServiceImpl implements AcademyService {

    @Autowired
    private AcademyDao academyDao;

    @Override
    public Academy getAcademyByName(String academyName) {
        return academyDao.getAcademyByName(academyName);
    }

    @Override
    public List<String> getAllAcademyName() {
        return academyDao.getAllAcademyName();
    }

    @Transactional(propagation = Propagation.NESTED)
    @Override
    public boolean updateAcademyPeopleNum(List<Academy> academyList) {
        int result=0;
        for(Academy academy:academyList){
            result=result+academyDao.updateAcademyPeopleNum(academy);
        }
        if(result==academyList.size()){
            return true;
        }
        return false;
    }
}
