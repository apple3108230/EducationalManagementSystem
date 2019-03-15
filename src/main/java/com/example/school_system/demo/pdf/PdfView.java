package com.example.school_system.demo.pdf;


import com.example.school_system.demo.service.PdfService;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


public class PdfView extends AbstractPdfView {

    private PdfService pdfService;

    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {
        pdfService.printStudentStatusMsg(model,document,writer,request,response);
    }

    public PdfView(PdfService pdfService) {
        this.pdfService = pdfService;
    }
}
