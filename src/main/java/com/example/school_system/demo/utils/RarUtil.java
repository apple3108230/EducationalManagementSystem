package com.example.school_system.demo.utils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import java.io.*;

public class RarUtil {

    public static boolean unrar(String zipFilePath,String unZipFilePath) throws IOException, RarException {
        boolean flag=false;
        File unzipDir=new File(unZipFilePath);
        if(!unzipDir.exists()){
            unzipDir.mkdir();
        }
        Archive archive=new Archive(new FileInputStream(new File(zipFilePath)));
        FileHeader fileHeader=archive.nextFileHeader();
        while(fileHeader!=null){
            if(fileHeader.isDirectory()){
                fileHeader=archive.nextFileHeader();
                continue;
            }
            //getFileNameW()方法获取文件名不会乱码（中文）
            String fileName=fileHeader.getFileNameW();
            File out=new File(unZipFilePath+"\\"+fileName);
            if(!out.exists()){
                if(!out.getParentFile().exists()){
                    out.getParentFile().mkdir();
                }
                out.createNewFile();
            }
            FileOutputStream os=new FileOutputStream(out);
            archive.extractFile(fileHeader,os);
            os.close();
            fileHeader=archive.nextFileHeader();
            flag=true;
        }
        archive.close();
        return flag;
    }

}
