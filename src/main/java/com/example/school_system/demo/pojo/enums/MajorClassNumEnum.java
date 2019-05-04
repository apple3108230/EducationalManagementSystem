package com.example.school_system.demo.pojo.enums;

public enum MajorClassNumEnum {
    ONE("一", 1), TWO("二", 2), THREE("三", 3), FOUR("四", 4), FIVE("五", 5);
    private String chineseNum;
    private int arabicNum;

    public static String getChineseNum(int arabicNum){
        for(MajorClassNumEnum numEnum:MajorClassNumEnum.values()){
            if(numEnum.getArabicNum()==arabicNum){
                return numEnum.getChineseNum();
            }
        }
        return null;
    }

    private MajorClassNumEnum(String chineseNum, int arabicNum) {
        this.chineseNum = chineseNum;
        this.arabicNum = arabicNum;
    }

    public String getChineseNum() {
        return chineseNum;
    }

    public void setChineseNum(String chineseNum) {
        this.chineseNum = chineseNum;
    }

    public int getArabicNum() {
        return arabicNum;
    }

    public void setArabicNum(int arabicNum) {
        this.arabicNum = arabicNum;
    }
}
