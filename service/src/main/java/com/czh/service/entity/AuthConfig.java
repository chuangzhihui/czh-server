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
 * 登录安全配置(AuthConfig)表实体类
 *
 * @author makejava
 * @since 2025-06-05 22:33:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthConfig implements Serializable {
private static final long serialVersionUID=1L;
    
    @TableId(type=IdType.AUTO)
    private Integer id;
    //密码正则    
    private String pwdReg;
    //连续密码错误次数后冻结账号    
    private Integer failNum;
    //清楚密码错误记录的时间(秒)    
    private Integer failNumTime;
    //密码正则描述    
    private String pwdRegDesc;
    //静默多久后登录失效(秒)    
    private Integer timeOut;
    //密码多少天后必须强制更换    
    private Integer passMax;
    //多少天开始提示用户修改密码    
    private Integer passWran;
    //是否开启自动备份    
    private Integer autoBackup;
    //备份数据得cron表达式    
    private String bakupDbCron;
    //上次编辑时间    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date atime;
}
