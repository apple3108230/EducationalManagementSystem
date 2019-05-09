package com.example.school_system.demo.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ExcelService {
    public void resolveExcelAndInsertScore(HttpServletResponse response, HttpServletRequest request, List<String> fileNames);
    public void resolveExcelAndInsertStudentStatusMsg(HttpServletResponse response,List<String> fileNames,HttpServletRequest request);
    public void resolveExcelAndInsertTeacherMsg(HttpServletResponse response,List<String> fileNames,HttpServletRequest request);
    public void resolveExcelAndInsertStudentMsg(HttpServletResponse response, List<String> fileNames,HttpServletRequest request) throws Exception;
}
