package com.czh.service.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录安全配置入参
 * <p>
 * 该 DTO 决定账号锁定策略与密码有效期，属于高风险写操作，
 * 此前完全无校验，现已补齐必填与合理区间。
 */
@Data
public class SetAuthConfigDto {
    //密码正则
    @NotBlank(message = "密码正则不能为空")
    @Size(max = 200, message = "密码正则不能超过200个字符")
    private String pwdReg;
    //密码正则描述
    @Size(max = 100, message = "密码规则描述不能超过100个字符")
    private String pwdRegDesc;

    //连续密码错误次数后冻结账号
    @NotNull(message = "密码错误锁定次数不能为空")
    @Min(value = 1, message = "密码错误锁定次数至少为1次")
    @Max(value = 100, message = "密码错误锁定次数不能超过100次")
    private Integer failNum;
    //清除密码错误记录的时间(秒)
    @NotNull(message = "错误记录清除时间不能为空")
    @Min(value = 1, message = "错误记录清除时间至少为1秒")
    private Integer failNumTime;

    //静默多久后登录失效(秒)
    @NotNull(message = "登录失效时间不能为空")
    @Min(value = 60, message = "登录失效时间至少为60秒")
    private Integer timeOut;
    //密码多少天后必须强制更换
    @NotNull(message = "密码强制更换天数不能为空")
    @Min(value = 1, message = "密码强制更换天数至少为1天")
    private Integer passMax;
    //是否开启自动备份 0不开启 1开启
    @NotNull(message = "是否开启自动备份不能为空")
    @Min(value = 0, message = "自动备份开关取值只能为0或1")
    @Max(value = 1, message = "自动备份开关取值只能为0或1")
    private Integer autoBackup;
    //备份数据得cron表达式
    @Size(max = 100, message = "备份cron表达式不能超过100个字符")
    private String bakupDbCron;
    //多少天开始提示用户修改密码
    @NotNull(message = "密码过期提醒天数不能为空")
    @Min(value = 1, message = "密码过期提醒天数至少为1天")
    private Integer passWran;
}
