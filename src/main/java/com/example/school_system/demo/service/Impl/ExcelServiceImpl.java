package com.example.school_system.demo.service.Impl;

import com.example.school_system.demo.exception.OtherException;
import com.example.school_system.demo.exception.ResolveExcelException;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.*;
import com.example.school_system.demo.utils.ScoreUtil;
import com.example.school_system.demo.utils.StringUtil;
import com.example.school_system.demo.utils.TimeUtil;
import com.example.school_system.demo.utils.WebUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentStatusMsgService studentStatusMsgService;
    @Autowired
    private TeacherMsgSerivce teacherMsgSerivce;
    @Autowired
    private StudentPersonalMsgService studentPersonalMsgService;
    @Autowired
    private MajorClassService majorClassService;
    @Autowired
    private BaseService baseService;
    @Autowired
    private AcademyService academyService;
    @Autowired
    private LogService logService;
    @Autowired
    private MajorService majorService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private TimestableService timestableService;

    @Value("${excel.save-path}")
    private String excelSavePath;
    @Value("${student.account.original-password}")
    private String STUDENT_ORIGINAL_PASSWORD;
    @Value("${teacher.account.original-password}")
    private String TEACHER_ORIGINAL_PASSWORD;

    /**
     * 判断是excel文件的格式
     * @param fileName
     * @return
     */
    private boolean isXls(String fileName){
        if(fileName.endsWith(".xls")){
            return true;
        }
        return false;
    }

    /**
     *判断excel文件后缀名来生成对应的Workbook类
     * @param isXls 后缀名是否为Xls
     * @param fileName excel文件名
     * @return
     * @throws IOException
     */
    private Workbook getWorkBook(boolean isXls,String fileName) throws IOException {
        Workbook workbook=null;
        if(isXls){
            workbook=new HSSFWorkbook(new FileInputStream(excelSavePath+"/"+fileName));
        }else {
            workbook=new XSSFWorkbook(new FileInputStream(excelSavePath+"/"+fileName));
        }
        return workbook;
    }


    /**
     * 解析完教师上传的EXCEL文件后进行更新数据库
     * @param response
     * @param fileNames 教师上传的所有文件名
     * @throws IOException
     */
    @Override
    public void resolveExcelAndInsertScore(HttpServletResponse response, HttpServletRequest request,List<String> fileNames){
        List<StudentScore> scoreBatchList=new ArrayList<>();
        fileNames.forEach(fileName->{
            Workbook workbook=null;
            //不同版本的excel文件需要的生成的workbook不一样，所以需要进行判断
            try{
                workbook=getWorkBook(isXls(fileName),fileName);
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
                            List<Course> courses=courseService.getCourseByCondition("",score.getCourse(),"","");
                            if(courses.size()==0){
                                try {
                                    throw new ResolveExcelException("找不到考试科目！");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            score.setCourseType(courses.get(0).getClassType());
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
        boolean isInsert=teacherService.insertScoreByStudentId(scoreBatchList);
        JSONObject json=new JSONObject();
        if(isInsert){
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            StringBuffer allFileName=new StringBuffer();
            fileNames.forEach(fileName->{
                allFileName.append(fileName).append(" ");
            });
            //写入敏感操作日志
            sensitiveOperation.setAction("上传文件:"+allFileName.toString());
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            User user= (User) request.getSession().getAttribute("user");
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","上传成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","上传失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @Override
    public void resolveExcelAndInsertStudentStatusMsg(HttpServletResponse response, List<String> fileNames,HttpServletRequest request) {
        List<StudentStatusMsg> studentStatusMsgList=new ArrayList<>();
        fileNames.forEach(fileName->{
            Workbook workbook=null;
            //不同版本的excel文件需要的生成的workbook不一样，所以需要进行判断
            try{
                workbook=getWorkBook(isXls(fileName),fileName);
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
                        //遍历excel当前行的所有列并以遍历pojo属性的方式写入pojo中
                        StudentStatusMsg studentStatusMsg=new StudentStatusMsg();
                        Class clz=studentStatusMsg.getClass();
                        Field[] fields=clz.getDeclaredFields();
                        for(int k=0;k<fields.length;k++){
                            Field field=fields[k];
                            field.setAccessible(true);
                            row.getCell(k).setCellType(Cell.CELL_TYPE_STRING);
                            try {
                                field.set(studentStatusMsg,row.getCell(k).getStringCellValue());
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        studentStatusMsgList.add(studentStatusMsg);
                    }
                }
            }
        });
        boolean isInsert=studentStatusMsgService.insertBatchStudentStatusMsg(studentStatusMsgList);
        JSONObject json=new JSONObject();
        if(isInsert){
            StringBuffer allFileName=new StringBuffer();
            fileNames.forEach(fileName->{
                allFileName.append(fileName).append(" ");
            });
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("上传文件：文件名："+allFileName);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","上传成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","上传失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @Override
    public void resolveExcelAndInsertTeacherMsg(HttpServletResponse response, List<String> fileNames,HttpServletRequest request) {
        List<TeacherMsg> teacherMsgList=new ArrayList<>();
        fileNames.forEach(fileName->{
            Workbook workbook=null;
            //不同版本的excel文件需要的生成的workbook不一样，所以需要进行判断
            try{
                workbook=getWorkBook(isXls(fileName),fileName);
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
                        String academyName=row.getCell(13).getStringCellValue();
                        TeacherMsg teacherMsg=new TeacherMsg();
                        teacherMsg.setId(getNewTeacherId(academyName));
                        Class clz=teacherMsg.getClass();
                        Field[] fields=clz.getDeclaredFields();
                        for(int k=1;k<fields.length;k++){
                            Field field=fields[k];
                            field.setAccessible(true);
                            row.getCell(k).setCellType(Cell.CELL_TYPE_STRING);
                            try {
                                field.set(teacherMsg,row.getCell(k-1).getStringCellValue());
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            creditTeahcerAccount(teacherMsg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        teacherMsgList.add(teacherMsg);
                    }
                }
            }
        });
        boolean isInsert=teacherMsgSerivce.insertBatchTeacherMsg(teacherMsgList);
        JSONObject json=new JSONObject();
        if(isInsert){
            StringBuffer allFileName=new StringBuffer();
            fileNames.forEach(fileName->{
                allFileName.append(fileName).append(" ");
            });
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("上传文件：文件名："+allFileName);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            json.put("message","上传成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","上传失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @Override
    public void resolveExcelAndInsertStudentMsg(HttpServletResponse response, List<String> fileNames,HttpServletRequest request) throws Exception {
        List<Student> studentList=new ArrayList<>();
        fileNames.forEach(fileName->{
            Workbook workbook=null;
            //不同版本的excel文件需要的生成的workbook不一样，所以需要进行判断
            try{
                workbook=getWorkBook(isXls(fileName),fileName);
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
                        Student student=new Student();
                        String className=row.getCell(6).getStringCellValue();
                        String classId=majorClassService.getMajorClassByCondition("",className).get(0).getId();
                        student.setId(getNewStudentId(classId));
                        Class clz=student.getClass();
                        Field[] fields=clz.getDeclaredFields();
                        for(int k=1;k<fields.length;k++){
                            Field field=fields[k];
                            field.setAccessible(true);
                            row.getCell(k).setCellType(Cell.CELL_TYPE_STRING);
                            try {
                                field.set(student,row.getCell(k-1).getStringCellValue());
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            creditStudentAccount(student);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        studentList.add(student);
                    }
                }
            }
        });
        boolean isInsert=studentPersonalMsgService.insertBatchStudentPersonalMessage(studentList);
        JSONObject json=new JSONObject();
        if(isInsert){
            StringBuffer allFileName=new StringBuffer();
            fileNames.forEach(fileName->{
                allFileName.append(fileName).append(" ");
            });
            User user= (User) request.getSession().getAttribute("user");
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            sensitiveOperation.setAction("上传文件：文件名："+allFileName);
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
            //更新学院人数
            updateAcademyPeopleNum();
            //更新专业人数
            updateMajorPeopleNum();
            json.put("message","上传成功！");
            WebUtil.printJSON(json.toJSONString(),response);
        }else{
            json.put("message","上传失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    @Override
    public void resolveExcelAndInsertTimestable(HttpServletResponse response, List<String> fileNames, HttpServletRequest request) {
        List<Timestable> timestableList=new ArrayList<>();
        fileNames.forEach(fileName->{
            Workbook workbook=null;
            //不同版本的excel文件需要的生成的workbook不一样，所以需要进行判断
            try{
                workbook=getWorkBook(isXls(fileName),fileName);
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
                        Timestable timestable=new Timestable();
                        Class clz=timestable.getClass();
                        Field[] fields=clz.getDeclaredFields();
                        for(int k=1;k<fields.length;k++){
                            Field field=fields[k];
                            field.setAccessible(true);
                            row.getCell(k).setCellType(Cell.CELL_TYPE_STRING);
                            try {
                                field.set(timestable,row.getCell(k-1).getStringCellValue());
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        timestableList.add(timestable);
                    }
                }
            }
        });
        boolean isInsert=timestableService.insertBatchTimestable(timestableList);
        JSONObject json=new JSONObject();
        if(isInsert){
            json.put("message","上传成功！");
            WebUtil.printJSON(json.toJSONString(),response);
            SensitiveOperation sensitiveOperation=new SensitiveOperation();
            User user= (User) request.getSession().getAttribute("user");
            sensitiveOperation.setAction("上传课程表");
            sensitiveOperation.setTime(TimeUtil.getNowTime());
            sensitiveOperation.setOperator(user.getUsername());
            logService.insertSensitiveOperationLog(sensitiveOperation);
        }else{
            json.put("message","上传失败！");
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

    /**
     * 生成学生的班级，生成新的学生id
     * @param classId
     * @return
     */
    private String getNewStudentId(String classId){
        String lastStudentId=studentPersonalMsgService.getLastStudentIdByClassId(classId+"%");
        String newStudentId;
        //若班级还没有添加学生，则此学生为第一个；否则+1
        if(lastStudentId.isEmpty()&&lastStudentId==null){
            newStudentId=lastStudentId+"01";
        }else{
            newStudentId="0"+String.valueOf(Integer.valueOf(lastStudentId)+1);
        }
        return newStudentId;
    }

    /**
     * 添加学生账号信息
     * @param student
     * @throws Exception
     */
    private void creditStudentAccount(Student student) throws Exception {
        String id=student.getId();
        String username=student.getName();
        String role="student";
        String permission="student";
        ByteSource byteSource=ByteSource.Util.bytes(username);
        String salt=byteSource.toString();
        int hashInterations=1024;
        String password=new SimpleHash("MD5",STUDENT_ORIGINAL_PASSWORD,byteSource,hashInterations).toString();
        User user=new User(id,username,password,role,permission,"",salt);
        boolean isInsert=baseService.insertUser(user);
        if(!isInsert){
            throw new Exception("创建账号失败！");
        }
    }

    /**
     * 生成新的教师id
     * @param academyName
     * @return
     */
    private String getNewTeacherId(String academyName){
        String academyId=academyService.getAcademyByName(academyName).getId();
        String lastTeacherId=teacherMsgSerivce.getLastTeacherId(academyId+"%");
        StringBuffer newTeacherId=new StringBuffer();
        if(lastTeacherId.isEmpty()||lastTeacherId==null){
            newTeacherId.append(academyId).append("0001");
        }else{
            String temp=String.valueOf(Integer.valueOf(academyId)+1);
            newTeacherId.append(temp);
            String zero="";
            for(int numOfZero=temp.length();numOfZero<6;numOfZero++){
                zero=zero+"0";
            }
            newTeacherId.insert(2,zero);
        }
        return newTeacherId.toString();
    }

    /**
     * 添加教师账号
     * @param teacherMsg
     */
    private void creditTeahcerAccount(TeacherMsg teacherMsg) throws Exception {
        String username=teacherMsg.getName();
        String role="teacher";
        String permission="teacher";
        ByteSource byteSource=ByteSource.Util.bytes(username);
        String salt=byteSource.toString();
        int hashInterations=1024;
        String password=new SimpleHash("MD5",TEACHER_ORIGINAL_PASSWORD,byteSource,hashInterations).toString();
        User user=new User(username,password,salt,role,permission,"",salt);
        boolean isInsert=baseService.insertUser(user);
        if(!isInsert){
            throw new Exception("创建账号失败！");
        }
    }

    /**
     * 更新学院人数
     * @throws Exception
     */
    private void updateAcademyPeopleNum() throws Exception {
       List<Academy> academyList=studentPersonalMsgService.countAcademyPeopleNum();
       boolean isUpdate=academyService.updateAcademyPeopleNum(academyList);
       if(!isUpdate){
           throw new OtherException("更新学院人数失败！");
       }
    }

    /**
     * 更新专业人数
     * @throws Exception
     */
    private void updateMajorPeopleNum() throws Exception {
       List<Major> majorList=studentPersonalMsgService.countMajorPeopleNum();
       boolean isUpdate=majorService.updateMajorPeopleNum(majorList);
       if(!isUpdate){
           throw new OtherException("更新专业人数失败！");
       }
    }
}
