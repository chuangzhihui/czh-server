package com.czh.service.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddSetting {
    @NotNull(message = "配置名称不能为空")
    @NotBlank(message = "配置名称不能为空")
    //配置名称
    private String title;
    private int type;//1 文本 2数字  3图片  4图文
    private String value;
    private int canDel;//是否允许删除这个配置
}
