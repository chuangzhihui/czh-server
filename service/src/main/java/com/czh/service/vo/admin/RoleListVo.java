package com.czh.service.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色列表出参
 * <p>
 * 不再直接返回 Role 实体，字段由本 VO 显式声明；
 * 后续 Role 实体新增字段不会自动外泄到接口。
 * <p>
 * 注：{@code ids}（权限 id 串）前端列表编辑时需要，故保留。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleListVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer roleId;

    private String roleName;

    /** 权限菜单 id 串 */
    private String ids;

    private String describe;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date atime;
}
