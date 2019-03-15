package com.example.school_system.demo.pojo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
/**
 * @param id 学号
 * @param student_id 考生号
 * @param id_num 证件号码
 * @param zz_face 政治面貌
 * @param sy_province 生源地
 * @param academy 学院
 * @param xuezhi 学制
 * @param cengci 层次
 */
public class Student_status_msg {
    private String id;
    private String name;
    private String sex;
    private String birthday;
    private String nation;
    private String id_num;
    private String zz_face;
    private String home_addr;
    private String tel;
    private String email;
    private String jiguan;
    private String student_exam_id;
    private String sy_province;
    private String academy;
    private String major;
    private String stu_class;
    private String xuezhi;
    private String cengci;
    private String join_party_time;
    private String birth_place;
    private String postal_code;
    private String student_type;
    private String student_img_url;


    private Map<String,Object> toMap(){
        Map<String,Object> map=new LinkedHashMap<String,Object>();
        map.put("学号",id);
        map.put("姓名",name);
        map.put("性别",sex);
        map.put("出生日期",birthday);
        map.put("出生地",birth_place);
        map.put("民族",nation);
        map.put("证件号码",id_num);
        map.put("入党时间",join_party_time);
        map.put("政治面貌",zz_face);
        map.put("家庭地址",home_addr);
        map.put("邮政编码",postal_code);
        map.put("联系电话",tel);
        map.put("电子邮箱",email);
        map.put("籍贯",jiguan);
        map.put("考生类型",student_type);
        map.put("考生号",student_exam_id);
        map.put("生源地",sy_province);
        map.put("学院",academy);
        map.put("专业",major);
        map.put("班级",stu_class);
        map.put("学制",xuezhi);
        map.put("层次",cengci);
        return map;
    }

    public Map<String,Object> getMap(){
        return toMap();
    }
}
