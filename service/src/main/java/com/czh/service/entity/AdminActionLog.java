package com.czh.service.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
public class AdminActionLog {
    @MongoId
    private String id;
    //操作名称
    private String operation;
    //操作地址
    private String method;
    //操作时间
    private String createTime;
    //操作用户
    private String userName;
    //用户id
    @Indexed
    private Integer userID;
    //请求数据
    private String params;
    //操作ip
    private String ip;
    //返回值
    private String bake;
}
