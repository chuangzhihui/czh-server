package com.czh.service.dto.admin;

import lombok.Data;

@Data
public class AddSetting {
    private String title;
    private int type;//1 文本 2数字  3图片  4图文
    private String value;
    private int canDel;//是否允许删除这个配置
}
