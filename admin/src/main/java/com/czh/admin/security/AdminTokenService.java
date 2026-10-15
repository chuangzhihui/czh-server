package com.czh.admin.security;

import com.czh.common.utils.JWTUtil;
import com.czh.common.utils.RedisUtil;
import com.czh.common.utils.RequestUtil;
import com.czh.service.entity.AuthConfig;
import com.czh.service.service.AuthConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录令牌服务：负责签发 JWT 并写入 Redis 登录态。
 * <p>
 * BE-06：签名密钥已从「管理员的密码哈希」改为独立配置项 {@code czh.admin-token.secret}，
 * 默认值配置在 admin/application.yaml。令牌有效期以 Redis TTL 为准。
 */
@Slf4j
@Service
public class AdminTokenService {

    /** Redis 中登录态 key 前缀，JWTAuthFilter 按同样规则回查 */
    private static final String ADMIN_TOKEN_PREFIX = "adminToken_";

    /**
     * 管理员令牌的 HMAC 签名密钥（BE-06）。
     * <p>
     * 历史上这里把「管理员的密码哈希」当签名密钥——密钥与敏感数据同源，
     * 库一泄露攻击面就翻倍。现改为独立配置项 {@code czh.admin-token.secret}，
     * 默认值见 admin/application.yaml，生产部署可在环境变量里覆盖同名配置。
     * <p>
     * 说明：JWTAuthFilter 对管理员令牌只 decode 取字段 + 与 Redis 登录态做等值比对，不做验签，
     * 因此更换签名密钥<b>不会</b>让已登录会话立即失效（Redis 里的旧令牌仍等值匹配）；
     * 「改密码踢下线」改由 editPwd 显式吊销 Redis 登录态承担（见 {@link #revoke(Integer)}）。
     */
    @Value("${czh.admin-token.secret}")
    private String adminTokenSecret;

    private final RedisUtil redisUtil;
    private final AuthConfigService authConfigService;

    public AdminTokenService(RedisUtil redisUtil, AuthConfigService authConfigService) {
        this.redisUtil = redisUtil;
        this.authConfigService = authConfigService;
    }

    /**
     * 为已通过认证的管理员签发令牌，并按系统安全配置设定有效期
     */
    public String issue(AdminDetails principal) {
        String token = JWTUtil.getJWTToken(principal.getAdminId().toString(), adminTokenSecret, RequestUtil.getIp());
        AuthConfig authConfig = authConfigService.getById(1);
        Integer timeOut = authConfig.getTimeOut();
        redisUtil.setEx(ADMIN_TOKEN_PREFIX + principal.getAdminId(), token,
                timeOut == null ? 3600L : timeOut, TimeUnit.SECONDS);
        return token;
    }

    /**
     * 显式吊销某管理员的登录态（改密码 / 封禁等场景使用，BE-06 配套）
     */
    public void revoke(Integer adminId) {
        redisUtil.delete(ADMIN_TOKEN_PREFIX + adminId);
    }
}
