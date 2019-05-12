package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.dao.TimestableDao;
import com.example.school_system.demo.pojo.Timestable;
import com.example.school_system.demo.service.TimestableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimestableServiceImpl implements TimestableService {

    @Autowired
    private TimestableDao timestableDao;

    @Override
    public List<Timestable> getClassTimestable(String classId) {
        return timestableDao.getClassTimestable(classId);
    }

    @Override
    public List<Timestable> getClassRoomTimestable(String classroomName) {
        return timestableDao.getClassRoomTimestable(classroomName);
    }

    @Override
    public List<Timestable> getTimestableByTeacherNameAndTerm(String teacherName,String term) {
        return timestableDao.getTimestableByTeacherNameAndTerm(teacherName, term);
    }

    @Override
    public boolean insertBatchTimestable(List<Timestable> timestableList) {
        int result=timestableDao.insertBatchTimestable(timestableList);
        if(result>0){
            return true;
        }
        return false;
    }
}
