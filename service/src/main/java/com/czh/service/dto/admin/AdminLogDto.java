package com.czh.service.dto.admin;

import com.czh.common.dto.PageDto;
import lombok.Data;

@Data
public class AdminLogDto extends PageDto {
    private String desc;
    private Integer adminId;
    private String address;
    private String ip;
}
