package com.example.school_system.demo.service;




import com.example.school_system.demo.exception.UserException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface PdfService {
    public void printStudentStatusMsg(Map<String,Object> model,Document document, PdfWriter pdfWriter,HttpServletRequest request,HttpServletResponse response) throws UserException, IOException, DocumentException;
}
