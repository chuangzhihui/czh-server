package com.czh.service.dto.admin;

import lombok.Data;

@Data
public class SetAuthConfigDto {
    //密码正则
    private String pwdReg;
    //密码正则描述
    private String pwdRegDesc;

    //连续密码错误次数后冻结账号
    private Integer failNum;
    //清除密码错误记录的时间(秒)
    private Integer failNumTime;

    //静默多久后登录失效(秒)
    private Integer timeOut;
    //密码多少天后必须强制更换
    private Integer passMax;
    //是否开启自动备份 0不开启 1开启
    private Integer autoBackup;
    //备份数据得cron表达式
    private String bakupDbCron;
    //多少天开始提示用户修改密码
    private Integer passWran;
}
