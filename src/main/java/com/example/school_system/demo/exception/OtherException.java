package com.example.school_system.demo.exception;

import lombok.Data;

@Data
public class OtherException extends Exception {
    private String message;
    public OtherException(String message){
        this.message=message;
    }
}
