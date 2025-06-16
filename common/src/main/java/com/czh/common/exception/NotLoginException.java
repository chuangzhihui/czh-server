package com.czh.common.exception;

public class NotLoginException extends RuntimeException{
    public NotLoginException(){
        super("请先登录!");
    }
}
