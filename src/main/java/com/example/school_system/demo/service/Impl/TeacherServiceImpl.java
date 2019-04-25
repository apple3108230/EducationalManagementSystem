package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.controller.TeacherController;
import com.example.school_system.demo.dao.TeacherDao;
import com.example.school_system.demo.pojo.DeleteScoreInfo;
import com.example.school_system.demo.pojo.StudentScore;
import com.example.school_system.demo.pojo.TeacherMsg;
import com.example.school_system.demo.service.TeacherService;
import com.example.school_system.demo.utils.ScoreUtil;
import com.example.school_system.demo.utils.StringUtil;
import com.example.school_system.demo.utils.WebUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherDao teacherDao;
    @Autowired
    private TeacherController teacherController;


    @Override
    public TeacherMsg getTeacherMsgById(String id) {
        return teacherDao.getTeacherMsgById(id);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.NESTED)
    public int insertScoreByStudentId(List<StudentScore> scoreList) {
        return teacherDao.insertScoreByStudentId(scoreList);
    }

    /**
     * 解析完教师上传的EXCEL文件后进行更新数据库
     * @param response
     * @param fileNames 教师上传的所有文件名
     * @throws IOException
     */
    @Override
    public void resolveExcelAndInsertScore(HttpServletResponse response, List<String> fileNames) throws IOException {
        List<StudentScore> scoreBatchList=new ArrayList<>();
        fileNames.forEach(fileName->{
            Workbook workbook=null;
            //不同版本的excel文件需要的生成的workbook不一样，所以需要进行判断
            try{
                if(fileName.endsWith(".xls")){
                    workbook=new HSSFWorkbook(new FileInputStream(teacherController.getSaveFilePath()+"/"+fileName));
                }
                if(fileName.endsWith(".xlsx")){
                    workbook=new XSSFWorkbook(new FileInputStream(teacherController.getSaveFilePath()+"/"+fileName));
                }
            }catch (IOException e){
                JSONObject json=new JSONObject();
                json.put("message","上传失败！原因："+e.getMessage());
                WebUtil.printJSON(json.toJSONString(),response);
            }
            if(workbook==null){
                throw new NullPointerException("WorkBook is null");
            }else{
                //获取工作表的数量
                int numOfSheet=workbook.getNumberOfSheets();
                for(int i=0;i<numOfSheet;i++){
                    Sheet sheet=workbook.getSheetAt(i);
                    int lastRowNum=sheet.getLastRowNum();
                    //第一行是标题，所以跳过第一行直接从第二行获取
                    for(int j=1;j<=lastRowNum;j++){
                        Row row=sheet.getRow(j);
                        StudentScore score=new StudentScore();
                        //考试科目名
                        if(row.getCell(0)!=null){
                            //强制设置单元格的类型为string
                            row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
                            score.setCourse(row.getCell(0).getStringCellValue());
                        }
                        //学生学号
                        if(row.getCell(1)!=null){
                            row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
                            score.setStudentId(row.getCell(1).getStringCellValue());
                        }
                        //平时成绩 期末成绩 学分
                        if(row.getCell(2)!=null&&row.getCell(3)!=null&&row.getCell(4)!=null){
                            row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
                            score.setUsualScore(row.getCell(2).getStringCellValue());
                            row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
                            score.setExamScore(row.getCell(3).getStringCellValue());
                            row.getCell(4).setCellType(Cell.CELL_TYPE_STRING);
                            score.setCredit(row.getCell(4).getStringCellValue());
                            //总成绩
                            score.setTotalScore(ScoreUtil.countTotalScore(score.getUsualScore(),score.getExamScore()));
                            //绩点
                            score.setGpa(ScoreUtil.countGPA(score.getTotalScore()));
                            //学分绩点
                            score.setCreditGpa(ScoreUtil.countCreditGpa(score.getCredit(),score.getGpa()));
                            score.setId(StringUtil.CustomUUID());
                        }
                        //学期学年
                        if(row.getCell(5)!=null){
                            row.getCell(5).setCellType(Cell.CELL_TYPE_STRING);
                            score.setTerm(row.getCell(5).getStringCellValue());
                        }
                        scoreBatchList.add(score);
                    }
                }
            }
        });
        teacherDao.insertScoreByStudentId(scoreBatchList);
    }

    @Override
    public boolean updateStudentScoreByCourseAndStudentId(StudentScore studentScore) {
        int result=teacherDao.updateStudentScoreByCourseAndStudentId(studentScore);
        if(result>=0){
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteStudentScoreByCourseAndStudentId(DeleteScoreInfo deleteScoreInfo) {
        int result=teacherDao.deleteStudentScoreByCourseAndStudentId(deleteScoreInfo);
        if(result>=0){
            return true;
        }
        return false;
    }


}
