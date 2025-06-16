package com.czh.common.utils.tosUtil.entity;

import lombok.Data;

@Data
public class TosEntity {
    /**
     * 空间名称
     */
    private String bucket;
    /**
     * 地域
     */
    private String region;
    /**
     * 节点
     */
    private String endpoint;
    private String accessKey;
    private String secretKey;
    /**
     * 自定义域名
     */
    private String domain;
}
