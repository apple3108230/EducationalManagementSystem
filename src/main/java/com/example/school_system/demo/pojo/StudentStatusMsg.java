package com.example.school_system.demo.pojo;

import com.example.school_system.demo.utils.StringUtil;
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
public class StudentStatusMsg {
    private String id;
    private String name;
    private String sex;
    private String birthday;
    private String nation;
    private String idNum;
    private String zzFace;
    private String homeAddr;
    private String tel;
    private String email;
    private String jiguan;
    private String studentExamId;
    private String syProvince;
    private String academy;
    private String major;
    private String stuClass;
    private String xuezhi;
    private String cengci;
    private String joinPartyTime;
    private String birthPlace;
    private String postalCode;
    private String studentType;
    private String studentImgUrl;


    private Map<String,Object> toMap(){
        Map<String,Object> map=new LinkedHashMap<String,Object>();
        map.put("学号", StringUtil.formatIdString(id));
        map.put("姓名",name);
        map.put("性别",sex);
        map.put("出生日期",birthday);
        map.put("出生地",birthPlace);
        map.put("民族",nation);
        map.put("证件号码",idNum);
        map.put("入党时间",joinPartyTime);
        map.put("政治面貌",zzFace);
        map.put("家庭地址",homeAddr);
        map.put("邮政编码",postalCode);
        map.put("联系电话",tel);
        map.put("电子邮箱",email);
        map.put("籍贯",jiguan);
        map.put("考生类型",studentType);
        map.put("考生号",studentExamId);
        map.put("生源地",syProvince);
        map.put("学院",academy);
        map.put("专业",major);
        map.put("班级",stuClass);
        map.put("学制",xuezhi);
        map.put("层次",cengci);
        return map;
    }

    public Map<String,Object> getMap(){
        return toMap();
    }
}
