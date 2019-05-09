package com.example.school_system.demo.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {
    /**
     * 解压zip
     * @param zipFilePath zip文件路径
     * @param unzipPath 解压路径
     * @return
     * @throws IOException
     */
    public static boolean unzip(String zipFilePath,String unzipPath) throws IOException {
        boolean flag=false;
        String[] strArray=zipFilePath.split("\\\\");
        String zipFileName=strArray[strArray.length-1].split(".zip")[0];
        File zipFile=new File(zipFilePath);
        File unZipFile=new File(unzipPath);
        if(!unZipFile.exists()){
            unZipFile.mkdir();
        }
        ZipFile zip=new ZipFile(zipFile, Charset.forName("GBK"));
        Enumeration enumeration=zip.entries();
        while(enumeration.hasMoreElements()){
            ZipEntry entry= (ZipEntry) enumeration.nextElement();
            String zipName=entry.getName();
            InputStream in=zip.getInputStream(entry);
            String outPath=unzipPath+"\\"+zipFileName+"\\"+zipName;
            File file = new File(outPath.split("\\\\"+zipName)[0]);
            if(!file.exists()){
                file.mkdirs();
            }
            OutputStream out=new FileOutputStream(outPath);
            byte[] bufferByte=new byte[1024];
            int len=0;
            while((len=in.read(bufferByte))>0){
                out.write(bufferByte,0,len);
            }
            in.close();
            out.close();
            flag=true;
        }
        zip.close();
        return flag;
    }

}
