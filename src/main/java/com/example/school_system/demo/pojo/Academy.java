package com.example.school_system.demo.pojo;

public class Academy {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column academy.id
     *
     * @mbg.generated Mon Apr 08 22:27:50 CST 2019
     */
    private String id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column academy.academy_name
     *
     * @mbg.generated Mon Apr 08 22:27:50 CST 2019
     */
    private String academyName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column academy.people_num
     *
     * @mbg.generated Mon Apr 08 22:27:50 CST 2019
     */
    private Integer peopleNum;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column academy.id
     *
     * @return the value of academy.id
     *
     * @mbg.generated Mon Apr 08 22:27:50 CST 2019
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column academy.id
     *
     * @param id the value for academy.id
     *
     * @mbg.generated Mon Apr 08 22:27:50 CST 2019
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column academy.academy_name
     *
     * @return the value of academy.academy_name
     *
     * @mbg.generated Mon Apr 08 22:27:50 CST 2019
     */
    public String getAcademyName() {
        return academyName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column academy.academy_name
     *
     * @param academyName the value for academy.academy_name
     *
     * @mbg.generated Mon Apr 08 22:27:50 CST 2019
     */
    public void setAcademyName(String academyName) {
        this.academyName = academyName == null ? null : academyName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column academy.people_num
     *
     * @return the value of academy.people_num
     *
     * @mbg.generated Mon Apr 08 22:27:50 CST 2019
     */
    public Integer getPeopleNum() {
        return peopleNum;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column academy.people_num
     *
     * @param peopleNum the value for academy.people_num
     *
     * @mbg.generated Mon Apr 08 22:27:50 CST 2019
     */
    public void setPeopleNum(Integer peopleNum) {
        this.peopleNum = peopleNum;
    }
}