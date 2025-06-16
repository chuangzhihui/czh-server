package com.czh.admin.security;

import com.czh.service.entity.Admin;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    //密码
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
        return UserDetails.super.isEnabled();
    }
}
