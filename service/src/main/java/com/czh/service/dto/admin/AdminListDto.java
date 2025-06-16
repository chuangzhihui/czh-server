package com.czh.service.dto.admin;

import com.czh.common.dto.PageDto;
import lombok.Data;

@Data
public class AdminListDto extends PageDto {
    private String name;
    private int roleId;
}
