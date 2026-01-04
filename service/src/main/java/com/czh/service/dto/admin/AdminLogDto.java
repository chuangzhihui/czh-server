package com.czh.service.dto.admin;

import com.czh.common.dto.PageDto;
import lombok.Data;

@Data
public class AdminLogDto extends PageDto {
    //操作内容描述
    private String desc;
    //管理员ID
    private Integer adminId;
    //操作地址
    private String address;
    //操作ID
    private String ip;
    //操作开始时间
    private String stime;
    //操作结束时间-与开始时间成对出现
    private String etime;
}
