package com.example.school_system.demo.exception;

import lombok.Data;

@Data
public class UserException extends Exception{
    private String message;
    public UserException(String message){
        this.message=message;
    }
}
