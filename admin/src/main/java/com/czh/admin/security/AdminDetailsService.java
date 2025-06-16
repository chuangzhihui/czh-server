package com.czh.admin.security;

import com.czh.common.exception.ErrorException;
import com.czh.service.entity.Admin;
import com.czh.service.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AdminDetailsService implements UserDetailsService {
    @Autowired
    AdminService adminService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminService.getAdminByUsername(username);
        if (admin == null) {
            throw new UsernameNotFoundException("用户不存在!");
        }
        if(!admin.getStatus().equals(1))
        {
            throw new ErrorException("ALERT账号已被冻结，请联系管理员解除!");
        }
        return new AdminDetails(admin);
    }
}
