package com.czh.service.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;


@Setter
@Getter
public class AdminLoginDto implements Serializable {
    /**
     * 账号：登录只负责校验凭证，不重复校验"账号命名规则"，
     * 因此只要求非空 + 长度上限（防止超长串被拿去反复做哈希比对）
     */
    @NotBlank
    @Length(max = 64, message = "账号格式错误")
    private String username;//用户名
    /**
     * 密码：原先是 @Length(min=6,max=16)，但 EditAdminDto 改密码<b>没有长度约束</b>，
     * 一旦管理员设置了 16 位以上（或手工改库）的密码，就永远登不进来。
     * 登录环节不应再用"创建时"的强度规则过滤，去掉下限、只保留上限。
     */
    @NotNull
    @NotBlank
    @Length(max = 128, message = "密码格式错误")
    private String password;//密码
    @Length(min = 4,max = 4,message = "图形验证码错误")
    @NotNull
    @NotBlank
    private String code;//图形验证码
    @NotNull
    @NotBlank
    private String uuid;//图形验证码标识
}
