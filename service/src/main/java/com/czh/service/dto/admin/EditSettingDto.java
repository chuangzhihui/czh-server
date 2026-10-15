package com.czh.service.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EditSettingDto {
    @Min(value = 1, message = "配置ID不合法")
    private int id;
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称不能超过100个字符")
    private String title;
    @Min(value = 1, message = "配置类型不合法")
    @Max(value = 4, message = "配置类型不合法")//1 文本 2数字  3图片  4图文
    private int type;
    private String value;
}
