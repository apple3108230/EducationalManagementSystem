package com.example.school_system.demo.dao;

import com.example.school_system.demo.pojo.DeleteScoreInfo;
import com.example.school_system.demo.pojo.StudentScore;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface StudentScoreDao {
    List<String> selectStudentScoreTerm(String id);
    int insertScoreByStudentId(@Param("scoreList") List<StudentScore> scoreList);
    int checkScoreExist(@Param("studentId") String studentId,@Param("course") String course,@Param("term") String term);
    List<StudentScore> selectStudentScoresByCondition(@Param("conditionMap") Map<String,String> conditionMap);
    int updateStudentScoreByCourseAndStudentId(StudentScore studentScore);
    int deleteStudentScoreByCourseAndStudentId(DeleteScoreInfo deleteScoreInfo);
}
