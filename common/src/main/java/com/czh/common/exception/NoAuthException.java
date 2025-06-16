package com.czh.common.exception;

public class NoAuthException extends  RuntimeException{
    public NoAuthException(){
        super("没有操作权限!");
    }
}
