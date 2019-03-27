package com.example.school_system.demo.utils;

import com.example.school_system.demo.pojo.Times;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TimeUtil {

    private static ThreadLocal<DateFormat> threadLocal=new ThreadLocal<DateFormat>();
    private static final String DATA_FORMAT="yyyy-MM-dd HH:mm:ss";

    /**
     *由于SimpleDataFormat和DateFormat存在线程安全的问题，所以在这里改变一下获取SimpleDataFormat的方式
     */
    public static DateFormat getDataFormat(){
        DateFormat dataFormat=threadLocal.get();
        if(dataFormat==null){
            dataFormat=new SimpleDateFormat(DATA_FORMAT);
            threadLocal.set(dataFormat);
        }
        return dataFormat;
    }

    public static String formatData(Date date){
        return getDataFormat().format(date);
    }

    public static Date parse(String time) throws ParseException {
        return getDataFormat().parse(time);
    }

    /**
     * 用于判断当前时间是否在时间段内
     * @param startTime
     * @param endTime
     * @return false:不在时间段内 true:在时间段内
     * @throws ParseException
     */
    public static boolean isInTime(String startTime,String endTime) throws ParseException {
        Times times =new Times();
        if(startTime!=null&&!startTime.isEmpty()&&endTime!=null&&!endTime.isEmpty()){
            times.setStartTime(startTime);
            times.setEndTime(endTime);
            Date nowTime=parse(formatData(new Date()));
            if(nowTime.getTime()<parse(startTime).getTime()||nowTime.getTime()>parse(endTime).getTime()){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    /**
     * 解析时间字符串（仅限于处理格式为“yyyy-MM-dd HH:mm:ss”）
     * @param time  only"yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static Map<String,String> parseTimeString(String time){
        String[] strs=time.split(" ");
        String[] date=strs[0].split("-");
        String day=date[2].replace("0","");
        String month=date[1].replace("0","");
        String year=date[0];
        String[] timeStr=strs[1].split(":");
        String hour=null;
        if(timeStr[0].startsWith("0")){
            hour=timeStr[0].replace("0","");
        }else {
            hour = timeStr[0];
        }
        String minute=null;
        if(timeStr[1].startsWith("0")){
            minute=timeStr[1].replace("0","");
        }else{
            minute=timeStr[1];
        }
        String second=null;
        if(timeStr[2].startsWith("0")&&timeStr[2].endsWith("0")){
            second=timeStr[2];
        }
        if(timeStr[2].startsWith("0")&&!timeStr[2].endsWith("0")){
            second=timeStr[2].replace("0","");
        }else{
            second=timeStr[2];
        }
        Map<String,String> timeMap=new HashMap<String, String>();
        timeMap.put("year",year);
        timeMap.put("month",month);
        timeMap.put("day",day);
        timeMap.put("hour",hour);
        timeMap.put("minute",minute);
        timeMap.put("second",second);
        return timeMap;
    }

}
