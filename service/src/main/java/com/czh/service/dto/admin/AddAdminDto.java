package com.czh.service.dto.admin;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
public class AddAdminDto {
    @Length(min = 4,max = 12,message = "账号格式错误,为4ß-12位")
    private String userName;
    @Length(min = 6,max = 16,message = "密码格式错误,为6-16位")
    private String password;
    @Min(value = 0,message = "角色ID错误")
    private int roleId;
}
