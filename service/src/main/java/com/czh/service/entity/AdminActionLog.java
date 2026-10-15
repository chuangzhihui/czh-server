package com.czh.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 后台管理员操作日志表(AdminActionLog)实体类
 * 数据源：MySQL 表 czh_admin_action_log
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionLog implements Serializable {
    private static final long serialVersionUID = 1L;
    //主键id
    @TableId(type = IdType.AUTO)
    private Long id;
    //操作名称
    private String operation;
    //操作地址(类名-方法名)
    private String method;
    //操作时间
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    //操作用户
    private String userName;
    //管理员id(关联 czh_admin.adminId)
    private Integer adminId;
    //请求数据
    private String params;
    //操作ip
    private String ip;
    //返回值
    private String bake;
}
