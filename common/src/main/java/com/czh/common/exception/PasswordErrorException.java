package com.czh.common.exception;

public class PasswordErrorException extends RuntimeException{
    public PasswordErrorException(String msg){
        super(msg);
    }
}