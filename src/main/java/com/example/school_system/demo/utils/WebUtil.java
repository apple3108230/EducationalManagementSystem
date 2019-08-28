package com.example.school_system.demo.utils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import net.coobird.thumbnailator.Thumbnails;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Random;

public class WebUtil {

    /**
     *
     * @param request
     * @return 是否为ajax请求
     *
     */
    public static boolean isAjax(HttpServletRequest request){
        if(request.getHeader("X-Requested-With") != null && "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))){
            return true;
        }else{
            return false;
        }
    }

    /**
     *
     * @param result
     * @param response
     * 把信息输出到页面上
     */
    public static void printToWeb(String result, HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        try {
            PrintWriter out = response.getWriter();
            out.printf(result);
            out.flush();
            out.close();
        }catch (IOException exception){
            exception.printStackTrace();
        }
    }

    /**
     * 返回json数据到页面
     * @param result
     * @param response
     */
    public static void printJSON(String result,HttpServletResponse response){
        response.setContentType("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try{
            PrintWriter out=response.getWriter();
            out.printf(result);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 生成随机的验证码（6位）
     * @return
     */
    public static String CreatRandomCode(){
        Random random=new Random();
        String codeString="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int randomNum=codeString.length();
        StringBuffer randomCode=new StringBuffer();
        for(int i=0;i<6;i++){
            randomCode.append(codeString.charAt((random.nextInt(randomNum))));
        }
        return randomCode.toString();
    }

    /**
     * 按指定大小进行缩放图片
     * @param url 原图片地址
     * @param width 缩放后图片的宽
     * @param height 缩放后图片的高
     * @param toFile 缩放后保存的位置
     * @throws IOException
     */
    public static void ImageResize(String url,int width,int height,String toFile) throws IOException {
        Thumbnails.of(url).size(width,height).toFile(toFile);
    }

    /**
     *
     * @param pdfUrl 生成pdf的位置
     * @param response HttpServletResponse
     * @param request HttpServletRequest
     * @param context ServletContext
     * @throws SAXException
     */
    public static void createPDF(String pdfUrl, HttpServletResponse response, HttpServletRequest request, ServletContext context) throws SAXException {
        try {
            String htmlString=GenerateDataForHtml.getInstance().generate("/page/studentPage/student_status_msg",request,response,context);
            FileOutputStream outputStream=new FileOutputStream(pdfUrl);
            ITextRenderer renderer=new ITextRenderer();
            DocumentBuilder documentBuilder= DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document=documentBuilder.parse(new ByteArrayInputStream(htmlString.getBytes()));
            renderer.setDocument(document,null);
            //由于默认不支持中文 所以需要自己添加中文字体
            ITextFontResolver fontResolver=renderer.getFontResolver();
            fontResolver.addFont("C:\\Users\\Administrator\\Desktop\\schoolSys\\src\\main\\resources\\templates\\font\\simsun.ttc",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
            renderer.layout();
            //生成PDF文件
            renderer.createPDF(outputStream);
            outputStream.close();
        }catch (IOException e){
            JSONObject json=new JSONObject();
            json.put("message",e.getMessage());
            WebUtil.printJSON(json.toJSONString(),response);
        } catch (DocumentException e) {
            JSONObject json=new JSONObject();
            json.put("message",e.getMessage());
            WebUtil.printJSON(json.toJSONString(),response);
        } catch (ParserConfigurationException e) {
            JSONObject json=new JSONObject();
            json.put("message",e.getMessage());
            WebUtil.printJSON(json.toJSONString(),response);
        }
    }

}
