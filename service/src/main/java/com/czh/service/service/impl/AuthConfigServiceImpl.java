package com.czh.service.service.impl;

import com.czh.service.entity.AuthConfig;
import com.czh.service.dao.AuthConfigDao;
import com.czh.service.service.AuthConfigService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;



/**
 * 登录安全配置(AuthConfig)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Service
public class AuthConfigServiceImpl extends ServiceImpl<AuthConfigDao,AuthConfig> implements AuthConfigService {
    
}
