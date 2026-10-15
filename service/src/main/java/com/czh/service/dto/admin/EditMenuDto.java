package com.czh.service.dto.admin;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class EditMenuDto extends AddMenuDto{
    @Min(value = 1, message = "菜单ID不合法")
    private int id;
}
