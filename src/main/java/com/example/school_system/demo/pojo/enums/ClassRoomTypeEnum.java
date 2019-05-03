package com.example.school_system.demo.pojo.enums;

public enum ClassRoomTypeEnum {
    SMALL_CLASS_ROOM("小型教室",90),BIG_CLASS_ROOM("大型教室",150),THEATER("阶梯教室",300),COMPUTER_CLASS_ROOM("电脑教室",90);
    private String classroomType;
    private int peopleNum;

    private ClassRoomTypeEnum(String classroomType, int peopleNum) {
        this.classroomType = classroomType;
        this.peopleNum = peopleNum;
    }

    public static int getPeopleNum(String classroomType){
        for(ClassRoomTypeEnum classRoomType:ClassRoomTypeEnum.values()){
            if(classRoomType.getClassroomType()==classroomType){
                return classRoomType.peopleNum;
            }
        }
        return 0;
    }

    public String getClassroomType() {
        return classroomType;
    }

    public void setClassroomType(String classroomType) {
        this.classroomType = classroomType;
    }

    public int getPeopleNum() {
        return peopleNum;
    }

    public void setPeopleNum(int peopleNum) {
        this.peopleNum = peopleNum;
    }
}
