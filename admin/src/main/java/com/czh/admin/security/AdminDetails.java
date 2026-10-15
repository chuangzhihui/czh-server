package com.czh.admin.security;

import com.czh.service.cache.AdminAuthCache;
import com.czh.service.entity.Admin;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
public class AdminDetails implements UserDetails {
    private Integer adminId;
    //用户名
    private String userName;
    //密码（BCrypt 哈希）
    //注意：JWT 签名当前以该值为密钥，故字段保留；
    //但禁止序列化输出，避免随 SecurityContext 进入 Session 或响应体而泄露。
    @JsonIgnore
    private String password;
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
    //权限
    private List<String>permissions;
    public AdminDetails(Admin admin) {
        adminId = admin.getAdminId();
        userName = admin.getUserName();
        password = admin.getPassword();
        roleId = admin.getRoleId();
        avatar = admin.getAvatar();
        status = admin.getStatus();
        lastTryLoginTime = admin.getLastTryLoginTime();
        failNum = admin.getFailNum();
        lastChangePwdTime = admin.getLastChangePwdTime();
    }

    /**
     * 用鉴权缓存对象构造。
     * <p>
     * 用于请求鉴权链路（JWTAuthFilter）。缓存对象不含密码，这里 password 保持为 null——
     * 不影响鉴权：该链路只做「token 是否有效 + 有无权限」，不签发 token，
     * 签发在登录阶段由 AdminAuthenticationProvider 完成，那里持有完整的 Admin 实体。
     *
     * @param cache 管理员鉴权缓存
     */
    public AdminDetails(AdminAuthCache cache) {
        adminId = cache.getAdminId();
        userName = cache.getUserName();
        password = null;
        roleId = cache.getRoleId();
        avatar = cache.getAvatar();
        status = cache.getStatus();
        lastTryLoginTime = cache.getLastTryLoginTime();
        failNum = cache.getFailNum();
        lastChangePwdTime = cache.getLastChangePwdTime();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if(permissions != null){
            permissions.forEach(authorityName->authorities.add(new SimpleGrantedAuthority(authorityName)));
        }
        authorities.add(new SimpleGrantedAuthority("admin"));
        return authorities;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        // 0冻结 1正常；返回 false 时 Spring Security 自动抛 DisabledException，
        // 取代原先手写 if + 自定义异常的做法（判定现在由 AdminAuthenticationProvider 加载后交给框架）
        return Integer.valueOf(1).equals(status);
    }
}
