package com.czh.service.cache;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理员鉴权缓存对象。
 * <p>
 * 用途：{@code JWTAuthFilter} 每个请求都要加载管理员，用缓存替掉这条 DB 查询。
 * <p>
 * 关键取舍：<b>刻意不缓存 password</b>。
 * <ol>
 *   <li>密码哈希属于敏感凭据，不应落到 Redis（Redis 没有账号体系，任何能连上的人都能读）。</li>
 *   <li>本对象只服务于「已登录用户的请求鉴权」，这条链路不需要密码——
 *       签发 token 在登录阶段完成，那里走的是 DB 直查（见 AdminAuthenticationProvider）。</li>
 * </ol>
 * 因此本类是独立于 Admin 实体的窄视图：将来 Admin 加了新字段，
 * 不会自动进入缓存，也不会经由缓存意外外泄。
 *
 * @author czh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuthCache implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer adminId;
    //用户名
    private String userName;
    //角色id
    private Integer roleId;
    //头像
    private String avatar;
    //账号状态 0冻结 1正常
    private Integer status;
    //上次登录失败的时间
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastTryLoginTime;
    //连续登录失败次数
    private Integer failNum;
    //上次修改密码的时间
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastChangePwdTime;
}
