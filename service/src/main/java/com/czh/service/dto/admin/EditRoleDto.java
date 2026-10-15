package com.czh.service.dto.admin;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class EditRoleDto extends AddRoleDto{
    @Min(value = 1, message = "角色ID不合法")
    private int roleId;
}
