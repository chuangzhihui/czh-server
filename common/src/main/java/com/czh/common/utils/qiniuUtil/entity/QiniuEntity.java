package com.czh.common.utils.qiniuUtil.entity;

import lombok.Data;

@Data
public class QiniuEntity {

    /**
     * 七牛云AccessKey
     */
    private String accessKey;
    /**
     * 七牛云SecretKey
     */
    private String secretKey;
    /**
     * 空间名称
     */
    private String bucket;
    /**
     * 节点地址
     */
    private String endpoint;
    /**
     * 自定义域名
     */
    private String domain;
}
