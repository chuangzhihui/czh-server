package com.czh.service.dto.admin;


import com.czh.common.dto.PageDto;
import lombok.Data;

@Data
public class GetFileListDto extends PageDto {
    private String name;
    private int type;
    private int pid;
}
