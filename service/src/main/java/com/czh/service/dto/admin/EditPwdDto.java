package com.czh.service.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EditPwdDto {
    @NotBlank
    @NotNull
    //原密码
    private String oldPwd;
    @NotBlank
    @NotNull
    //新密码
    private String  password;
}
