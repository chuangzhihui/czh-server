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
 * 管理员表(Admin)表实体类
 *
 * @author makejava
 * @since 2025-06-05 22:33:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin implements  Serializable {
    private static final long serialVersionUID=1L;
    //管理员id    
    @TableId(type=IdType.AUTO)
    private Integer adminId;
    //用户名    
    private String userName;
    //密码    
    private String password;
    //最后登录ip    
    private String lastLoginIp;
    //最后登录时间    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;
    //角色id    
    private Integer roleId;
    //添加时间    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date atime;
    //头像    
    private String avatar;
    //账号状态 0冻结 1正常    
    private Integer status;
    //上次登录失败的IP    
    private String lastTryLoginIp;
    //上次登录失败的时间    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastTryLoginTime;
    //连续登录失败次数    
    private Integer failNum;
    //上次修改密码的时间    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastChangePwdTime;


}
