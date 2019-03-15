package com.example.school_system.demo.exception;

import lombok.Data;

@Data
public class SystemException extends Exception{
    private String message;
    public SystemException(String message){
        this.message=message;
    }
}
