package com.czh.common.utils.localUploadUtil.entity;

import lombok.Data;

@Data
public class LocalUploadConfig {
    //上传文件的基础目录 不要以斜线结尾
    private String path;
    //访问域名
    private String domain;
    //上传地址
    private String host;
}
