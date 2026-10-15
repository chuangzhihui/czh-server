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
        res.code=500;
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

    /**
     * 请求地址不存在：业务码 404，仅表示"地址不存在"，
     * 任何业务失败（含鉴权失败、token 过期）都不得复用它（见 BE-29）。
     */
    public static <T> JSONResult<T> notFound()
    {
        JSONResult<T> res=new JSONResult<>();
        res.code=404;
        res.msg="请求地址不存在";
        return res;
    }

    /**
     * 自定义业务码。用于错误出口需要按真实错误类型分类返回的场景（见 BE-29）。
     *
     * @param code 业务码（200/400/401/403/404/500）
     * @param msg  提示文案
     */
    public static <T> JSONResult<T> fail(int code, String msg)
    {
        JSONResult<T> res=new JSONResult<>();
        res.code=code;
        res.msg=msg;
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
