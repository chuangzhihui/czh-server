package com.czh.service.dto.admin;

import com.czh.common.dto.PageDto;
import lombok.Data;

@Data
public class DbBackUpListDto extends PageDto {
    private String stime;
    private String etime;
    private Integer adminId;
}
