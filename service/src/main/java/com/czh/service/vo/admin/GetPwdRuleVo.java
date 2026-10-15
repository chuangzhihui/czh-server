package com.czh.service.vo.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * 密码规则出参
 * <p>
 * 只暴露密码规则相关字段，不再返回整个 AuthConfig
 * （避免把失败锁定次数、静默时长、备份 cron 等安全策略泄露给所有登录用户）。
 */
@Data
public class GetPwdRuleVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 密码正则表达式 */
    private String pwdReg;
    /** 密码正则描述（前端提示文案） */
    private String pwdRegDesc;
    /** 密码多少天后必须强制更换 */
    private Integer passMax;
    /** 多少天开始提示用户修改密码 */
    private Integer passWran;
}
