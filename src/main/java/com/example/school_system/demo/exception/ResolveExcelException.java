package com.example.school_system.demo.exception;

import lombok.Data;

@Data
public class ResolveExcelException extends Exception {

    private String message;
    public ResolveExcelException(String message){
        this.message=message;
    }

}
