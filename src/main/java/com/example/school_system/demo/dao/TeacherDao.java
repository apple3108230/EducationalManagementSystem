package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.DeleteScoreInfo;
import com.example.school_system.demo.pojo.StudentScore;
import com.example.school_system.demo.pojo.TeacherMsg;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface TeacherDao {
    public TeacherMsg getTeacherMsgById(String id);
    public int insertScoreByStudentId(@Param("scoreList") List<StudentScore> scoreList);
    public int checkScoreExist(@Param("studentId") String studentId,@Param("course") String course,@Param("term") String term);
    public List<StudentScore> selectStudentScoresByCondition(@Param("conditionMap") Map<String,String> conditionMap);
    public int updateStudentScoreByCourseAndStudentId(StudentScore studentScore);
    public int deleteStudentScoreByCourseAndStudentId(DeleteScoreInfo deleteScoreInfo);
}
