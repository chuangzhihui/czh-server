package com.czh.service.vo.admin;

import lombok.Data;

@Data
public class GetSettingListVo {
    private int id;
    private String title;
    private String value;
    private int type;
    private int canDel;
}
