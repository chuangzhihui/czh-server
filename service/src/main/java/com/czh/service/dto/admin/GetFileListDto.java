package com.czh.service.dto.admin;


import com.czh.common.dto.PageDto;
import lombok.Data;

@Data
public class GetFileListDto extends PageDto {
    private String name;
    private String types;//查询哪些文件多个逗号隔开
    private int pid;
}
