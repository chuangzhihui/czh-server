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
    @Length(min = 6,max = 12,message = "账号格式错误")
    @NotNull
    @NotBlank
    private String username;//用户名
    @Length(min = 6,max = 16,message = "密码格式错误")
    @NotNull
    @NotBlank
    private String password;//密码
    @Length(min = 4,max = 4,message = "图形验证码错误")
    @NotNull
    @NotBlank
    private String code;//图形验证码
    @NotNull
    @NotBlank
    private String uuid;//图形验证码标识
}
