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
    private String username;
    @Length(min = 6,max = 16,message = "密码格式错误")
    private String password;
    @Length(min = 4,max = 4,message = "图形验证码错误")
    private String code;
    @NotNull
    @NotBlank
    private String uuid;
}
