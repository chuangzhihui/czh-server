package com.czh.service.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理员操作日志列表出参
 * <p>
 * 不再直接返回 AdminActionLog 实体，字段由本 VO 显式声明。
 * 注：{@code params} 与 {@code bake} 落库前已由 SensitiveUtil 将
 * password / token 等敏感值置为 {@code ****}。
 */
@Data
public class AdminActionLogVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 操作名称 */
    private String operation;
    /** 请求的类-方法名 */
    private String method;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 操作账号 */
    private String userName;
    /** 管理员ID，关联 czh_admin.adminId */
    private Integer adminId;
    /** 请求参数（已脱敏） */
    private String params;
    /** 操作IP */
    private String ip;
    /** 返回值（已脱敏） */
    private String bake;
}
