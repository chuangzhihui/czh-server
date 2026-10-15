package com.czh.service.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
public class AddAdminDto {
    @Length(min = 4,max = 12,message = "账号格式错误,为4-12位")
    @NotNull
    @NotBlank
    private String userName;//账号
    @Length(min = 6,max = 16,message = "密码格式错误,为6-16位")
    @NotNull
    @NotBlank
    private String password;//密码
    @Min(value = 0,message = "角色ID错误")
    @NotNull
    //角色ID
    private Integer roleId;
}
