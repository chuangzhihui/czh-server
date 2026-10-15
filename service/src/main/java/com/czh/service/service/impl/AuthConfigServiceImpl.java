package com.czh.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.czh.common.utils.RedisUtil;
import com.czh.service.cache.AuthCacheKeys;
import com.czh.service.dao.AuthConfigDao;
import com.czh.service.entity.AuthConfig;
import com.czh.service.service.AuthConfigService;
import com.czh.service.vo.admin.GetPwdRuleVo;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;




/**
 * 登录安全配置(AuthConfig)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Service
@Slf4j
public class AuthConfigServiceImpl extends ServiceImpl<AuthConfigDao,AuthConfig> implements AuthConfigService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** AuthConfig 只有一行，主键固定为 1 */
    private static final Integer CONFIG_ID = 1;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public GetPwdRuleVo getPwdRule() {
        AuthConfig config = getById(CONFIG_ID);
        GetPwdRuleVo vo = new GetPwdRuleVo();
        if (config != null) {
            vo.setPwdReg(config.getPwdReg());
            vo.setPwdRegDesc(config.getPwdRegDesc());
            vo.setPassMax(config.getPassMax());
            vo.setPassWran(config.getPassWran());
        }
        return vo;
    }

    // ==================== 鉴权缓存 ====================

    @Override
    public AuthConfig getAuthConfigCache() {
        try {
            String cache = redisUtil.get(AuthCacheKeys.AUTH_CONFIG);
            if(cache != null && !cache.isEmpty() && !"null".equals(cache)){
                return MAPPER.readValue(cache, AuthConfig.class);
            }
        } catch (Exception e) {
            log.warn("安全配置缓存解析失败, 回源DB", e);
        }
        AuthConfig config = getById(CONFIG_ID);
        if(config == null){
            return null;
        }
        try {
            redisUtil.setEx(AuthCacheKeys.AUTH_CONFIG, MAPPER.writeValueAsString(config),
                    AuthCacheKeys.AUTH_CONFIG_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("安全配置缓存回填失败", e);
        }
        return config;
    }

    @Override
    public void evictAuthConfigCache() {
        redisUtil.delete(AuthCacheKeys.AUTH_CONFIG);
    }

    /**
     * 覆写通用更新：安全策略改动（失败次数、静默时长等）立即生效。
     * <p>
     * 与管理员同理，失效写在写方法里，避免调用方遗漏。
     */
    @Override
    public boolean updateById(AuthConfig entity) {
        boolean ok = super.updateById(entity);
        evictAuthConfigCache();
        return ok;
    }

    @Override
    public boolean update(AuthConfig entity, Wrapper<AuthConfig> updateWrapper) {
        boolean ok = super.update(entity, updateWrapper);
        evictAuthConfigCache();
        return ok;
    }
}
