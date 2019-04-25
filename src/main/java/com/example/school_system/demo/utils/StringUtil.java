package com.example.school_system.demo.utils;

import com.example.school_system.demo.pojo.StudentStatusMsg;

import java.util.Random;
import java.util.UUID;

public class StringUtil {

    /**
     * 此方法用于格式化指定id
     * 适用于在数据库中是0开头的id
     * @param id 需要格式化的id
     * @return
     */
    public static String formatIdString(String id){
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append(id).insert(0,"0");
        return stringBuffer.toString();
    }

    /**
     * 生成自定义格式的UUID的字符串
     * @return
     */
    public static String CustomUUID(){
        Random random=new Random();
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append(UUID.randomUUID().toString().replace("-", "").toLowerCase());
        return stringBuffer.toString();
    }


}
