package com.example.school_system.demo.pojo;

import lombok.Data;

@Data
public class StudentScorePo {
    private String course;
    private String studentId;
    private String usualScore;
    private String examScore;
    private String credit;
    private String term;

    public StudentScore toStudentScore(String totalScore,String gpa,String creditGpa){
        StudentScore score=new StudentScore();
        score.setCreditGpa(creditGpa);
        score.setCredit(getCredit());
        score.setTerm(getTerm());
        score.setGpa(gpa);
        score.setTotalScore(totalScore);
        score.setExamScore(getExamScore());
        score.setUsualScore(getUsualScore());
        score.setStudentId(getStudentId());
        score.setCourse(getCourse());
        return score;
    }
}
