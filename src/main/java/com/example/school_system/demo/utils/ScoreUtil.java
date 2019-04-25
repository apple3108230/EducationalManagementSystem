package com.example.school_system.demo.utils;

import java.text.DecimalFormat;

public class ScoreUtil {

    final private static double USUAL_SCORE_PERCENT=0.3;
    final private static double EXAM_SCORE_PERCENT=0.7;
    final private static DecimalFormat df=new DecimalFormat("#.0");

    /**
     * 公式：平时*USUAL_SCORE_PERCENT+期末*EXAM_SCORE_PERCENT
     * @param usualScore 平时成绩
     * @param examScore 期末成绩
     * @return 总成绩
     */
    public static String countTotalScore(String usualScore,String examScore){
        double usual=Double.valueOf(usualScore);
        double exam=Double.valueOf(examScore);
        double total=usual * USUAL_SCORE_PERCENT + exam * EXAM_SCORE_PERCENT;
        return df.format(total);
    }

    /**
     * 公式：若分数大于90，则按90计算；分数/10-5=绩点 若计算出来的绩点是负数的话，则直接返回0
     * @param totalScore 总百分比分数
     * @return 绩点
     */
    public static String countGPA(String totalScore){
        double total=Double.valueOf(totalScore);
        if(total>90.0){
            total=90.0;
        }
        double GPA=total/10-5;
        //保留一位小数
        if(df.format(GPA).startsWith("-")){
            return "0";
        }else{
            return df.format(GPA);
        }
    }

    /**
     * 公式：学分*绩点  若计算出来的结果为负数，则直接返回0
     * @param credit 学分
     * @param gpa 绩点
     * @return 学分绩点
     */
    public static String countCreditGpa(String credit,String gpa){
        double creditNum=Double.valueOf(credit);
        double gpaNum=Double.valueOf(gpa);
        double creditGpa=creditNum*gpaNum;
        if(df.format(creditGpa).startsWith("-")){
            return "0";
        }else{
            return df.format(creditGpa);
        }
    }


}
