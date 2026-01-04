package com.czh.service.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddRoleDto {
    @NotNull(message = "角色名称不能为空")
    @NotBlank(message = "角色名称不能为空")
    //角色名称
    private String roleName;
    @NotNull(message = "角色备注不能为空")
    @NotBlank(message = "角色备注不能为空")
    //角色备注
    private String describe;
    @NotNull(message = "角色权限不能为空")
    @NotBlank(message = "角色权限不能为空")
    //角色权限
    private String ids;
}
