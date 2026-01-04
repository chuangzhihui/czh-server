package com.czh.service.dto.admin;

import com.czh.common.dto.PageDto;
import lombok.Data;

@Data
public class AdminListDto extends PageDto {
    //昵称
    private String name;
    //角色ID
    private int roleId;
}
