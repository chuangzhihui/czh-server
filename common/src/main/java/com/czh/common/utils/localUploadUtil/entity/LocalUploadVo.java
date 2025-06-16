package com.czh.common.utils.localUploadUtil.entity;

import lombok.Data;

@Data
public class LocalUploadVo {
    /**
     * 文件名称
     */
    private String name;
    /**-
     * 访问url
     */
    private String url;
    /**
     * 文件key
     */
    private String key;
}
