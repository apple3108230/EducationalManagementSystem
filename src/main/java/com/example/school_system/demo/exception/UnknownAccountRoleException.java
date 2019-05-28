package com.example.school_system.demo.exception;

import org.apache.shiro.authc.AuthenticationException;

public class UnknownAccountRoleException extends AuthenticationException {
    public UnknownAccountRoleException(String message) {super(message); }
}
