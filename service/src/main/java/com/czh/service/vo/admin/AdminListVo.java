package com.czh.service.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AdminListVo{
    private Integer adminId;
    private Integer roleId;
    private String userName;
    private String lastLoginTime;
    private String lastLoginIp;
    private String roleName;
    private Integer status;//是否冻结
    private String  lastTryLoginIp;//上次登录失败的IP
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastTryLoginTime;//上次登录失败的时间
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastChangePwdTime;//上次修改密码的时间
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private String atime;
}
