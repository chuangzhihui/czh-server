package com.czh.common.utils.aliOssUtil.entity;

import lombok.Data;

@Data
public class AliOssEntity {
    private String bucket;
    private String endpoint;
    private String ak;
    private String sk;
    private String domain;
}
