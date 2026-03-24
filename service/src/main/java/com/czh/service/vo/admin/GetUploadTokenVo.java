package com.czh.service.vo.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetUploadTokenVo {
    @NotNull
    private String token;//上传地址
    @NotNull
    private Integer type;//上传类型 1七牛 2阿里云 3腾讯云 4本地 5火山云
    @NotNull
    private String host;//上传地址
}
