package com.czh.service.dto.admin;

import lombok.Data;

@Data
public class EditSettingDto {
    private int id;
    private String title;
    private int type;//1 文本 2数字  3图片  4图文
    private String value;
}
