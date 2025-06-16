package com.czh.service.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
public class EditAdminDto {
    @Min(value = 0,message = "管理员ID错误")
    @NotNull(message = "管理员ID不能为空")
    private Integer adminId;
    @Min(value = 0,message = "角色ID错误")
    @NotNull(message = "角色ID不能为空")
    private Integer roleId;

    private String password;
    @Length(min = 4,max = 12,message = "账号格式错误,为4-12位")
    private String userName;
}
