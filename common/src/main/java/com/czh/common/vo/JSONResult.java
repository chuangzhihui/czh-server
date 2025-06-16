package com.czh.common.vo;

import lombok.NonNull;

import java.io.Serializable;

public  class JSONResult<T>  implements Serializable {
    private static final long serialVersionUID = -8970718410437077606L;
    public int code;
    public String msg;
    public T data;
    public static   <T> JSONResult<T> success(String msg,T data){
        JSONResult<T> result=new JSONResult<T>();
        result.data=data;
        result.msg=msg;
        return result;
    }
    public static  <T> JSONResult<T> success(@NonNull String msg){
        JSONResult<T> res=new JSONResult<>();
        res.code=200;
        res.msg=msg;
        return res;
    }

    public static  <T> JSONResult<T> success(T data){
        JSONResult<T> res=new JSONResult<>();
        res.code=200;
        res.msg="操作成功";
        res.data=data;
        return res;
    }
    public static <T> JSONResult<T> success(){
        JSONResult<T> res=new JSONResult<>();
        res.code=200;
        res.msg="操作成功!";

        return res;
    }

    public static <T> JSONResult<T> error(String msg,T data){
        JSONResult<T> res=new JSONResult<>();
        res.code=1;
        res.msg=msg;
        res.data=data;
        return res;
    }
    public static <T> JSONResult<T> error(String msg){
        JSONResult<T> res=new JSONResult<>();
        res.code=500;
        res.msg=msg;
        return res;
    }
    public static <T> JSONResult<T> error(T data){
        JSONResult<T> res=new JSONResult<>();
        res.code=500;
        res.msg="操作失败!";
        res.data=data;
        return res;
    }
    public static <T> JSONResult<T> error(){
        JSONResult<T> res=new JSONResult<>();
        res.code=500;
        res.msg="操作失败!";
        return res;
    }

    public static <T> JSONResult<T> permissionDenied()
    {
        JSONResult<T> res=new JSONResult<>();
        res.code=403;
        res.msg="无权限!";
        return res;
    }
    public static <T> JSONResult<T> loginOff()
    {
        JSONResult<T> res=new JSONResult<>();
        res.code=401;
        res.msg="登录失效!";
        return res;
    }
}
