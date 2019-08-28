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
    final public static int TIME_IS_ILLEGAL=-2;
    final public static int STARTTIME_LESS_THAN_NOWTIME=-1;
    final public static int ENDTIME_LESS_THAN_STARTTIME=1;
    final public static int ENDTIME_LESS_THAN_NOWTIME=0;
    final public static int OK=2;

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
        String day;
        String month;
        String year=date[0];
        if(date[2].startsWith("0")){
            day=date[2].replace("0","");
        }else{
            day=date[2];
        }
        if(date[1].startsWith("0")){
            month=date[1].replace("0","");
        }else{
            month=date[1];
        }
        String[] timeStr=strs[1].split(":");
        String hour=null;
        if(timeStr[0].startsWith("0")&&timeStr[0].endsWith("0")){
            hour=timeStr[0];
        }else {
            hour = timeStr[0].replace("0","");
        }
        String minute=null;
        if(timeStr[1].startsWith("0")&&timeStr[1].endsWith("0")){
            minute=timeStr[1];
        }
        if(timeStr[1].startsWith("0")&&!timeStr[1].endsWith("0")){
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

    public static int checkTime(String startTime,String endTime) throws ParseException {
        Map<String,String> startTimeMap=null;
        if(startTime.isEmpty()){
            startTimeMap=parseTimeString(getNowTime());
        }else{
            startTimeMap= parseTimeString(startTime);
        }
        Map<String,String> endTimeMap= parseTimeString(endTime);
        if(!checkMonthAndDayIslegal(startTimeMap.get("year"),startTimeMap.get("month"),startTimeMap.get("day"))&&!checkMonthAndDayIslegal(endTimeMap.get("year"),endTimeMap.get("month"),endTimeMap.get("day"))){
            return TIME_IS_ILLEGAL;
        }
        SimpleDateFormat df= (SimpleDateFormat) getDataFormat();
        Date start=null;
        Date end=df.parse(endTime);
        Date now=df.parse(df.format(new Date()));
        if(!startTime.isEmpty()){
            start=df.parse(startTime);
            if(start.getTime()<=now.getTime()){
                return STARTTIME_LESS_THAN_NOWTIME;
            }
            if(end.getTime()<=start.getTime()){
                return ENDTIME_LESS_THAN_STARTTIME;
            }
        }
        if(end.getTime()<=now.getTime()){
            return ENDTIME_LESS_THAN_NOWTIME;
        }
        return OK;
    }

    public static String ParseTimeMapToSimpleCronExpression(Map<String,String> timeMap){
        Map<String,String> cronTimeMap=new HashMap<>();
        timeMap.forEach((key,value)->{
            if(key.equals("second")){
                if(value.startsWith("0")&&value.endsWith("0")){
                    String second="0";
                    cronTimeMap.put("second",second);
                }
                if(value.startsWith("0")&&!value.endsWith("0")){
                    String second=value.substring(1);
                    cronTimeMap.put("second",second);
                }
            } else if(key.equals("minute")){
                if(value.startsWith("0")&&value.endsWith("0")){
                    String minute="0";
                    cronTimeMap.put("minute",minute);
                }
                if(value.startsWith("0")&&!value.endsWith("0")){
                    String minute=value.substring(1);
                    cronTimeMap.put("minute",minute);
                }
            } else{
                cronTimeMap.put(key,value);
            }
        });
        String cronString=cronTimeMap.get("second")+" "+cronTimeMap.get("minute")+" "+cronTimeMap.get("hour")+" "+cronTimeMap.get("day")+" "+cronTimeMap.get("month")+" "+"?"+" "+cronTimeMap.get("year");
        return cronString;
    }

    public static boolean checkMonthAndDayIslegal(String year,String month,String day){
        String illegalDay=new String();
        switch (month){
            case "1":
            case "3":
            case "5":
            case "7":
            case "8":
            case "10":
            case "12":
                illegalDay="31";
                break;
            case "4":
            case "6":
            case "9":
            case "11":
                illegalDay="30";
                break;
            case "2":
                if(Integer.parseInt(year)%4==0){
                    illegalDay="29";
                    break;
                }else{
                    illegalDay="28";
                    break;
                }
        }
        if(day.equals(illegalDay)){
            return true;
        }
        return false;
    }

    /**
     * 获取当前时间
     * @return
     */
    public static String getNowTime(){
        return formatData(new Date());
    }

}
