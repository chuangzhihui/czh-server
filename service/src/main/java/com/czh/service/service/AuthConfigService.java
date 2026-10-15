package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.service.entity.AuthConfig;
import com.czh.service.vo.admin.GetPwdRuleVo;

/**
 * 登录安全配置(AuthConfig)表服务接口
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface AuthConfigService extends IService<AuthConfig> {

    /**
     * 获取密码规则
     * <p>
     * 只返回密码规则相关字段，不暴露失败锁定次数、静默时长、备份 cron 等安全策略。
     *
     * @return 密码规则出参
     */
    GetPwdRuleVo getPwdRule();

    /**
     * 获取登录安全配置（带缓存，key 见 {@link com.czh.service.cache.AuthCacheKeys#AUTH_CONFIG}）。
     * <p>
     * 每个请求都要读它来续期 token，所以用缓存替掉这条 DB 查询。
     * 写操作已在实现类中自动失效，配错最多影响 token 续期时长，不涉及越权。
     *
     * @return 安全配置；无记录时返回 null，调用方需判空
     */
    AuthConfig getAuthConfigCache();

    /**
     * 删除安全配置缓存。
     * <p>
     * 正常不需要外部调用：更新方法已自动失效。仅在直连数据库修改后手动补偿。
     */
    void evictAuthConfigCache();

}
