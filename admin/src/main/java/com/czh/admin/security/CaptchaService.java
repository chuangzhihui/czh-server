package com.czh.admin.security;

import com.czh.common.exception.LoginFailException;
import com.czh.common.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务：负责验证码的存取与一次性校验。
 * <p>
 * 原先由 LoginController 写入 Redis 后再没被读过，且 key 永不过期会一直堆积；
 * 这里收敛为"带 TTL 存储 + 校验后立即删除"，即一次性消费，杜绝同一个验证码被重放。
 */
@Slf4j
@Service
public class CaptchaService {

    /** Redis 中验证码 key 前缀 */
    private static final String CAPTCHA_KEY_PREFIX = "verify_code_";
    /** 验证码有效期（秒） */
    private static final long CAPTCHA_TTL_SECONDS = 5 * 60L;

    private final RedisUtil redisUtil;

    public CaptchaService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 保存验证码并返回它的 uuid 标识
     */
    public String create(String code) {
        String uuid = UUID.randomUUID().toString();
        redisUtil.setEx(CAPTCHA_KEY_PREFIX + uuid, code, CAPTCHA_TTL_SECONDS, TimeUnit.SECONDS);
        return uuid;
    }

    /**
     * 校验验证码：无论比对是否通过，都立即删除，保证只能用一次
     *
     * @throws LoginFailException 验证码已失效或填写错误
     */
    public void verify(String uuid, String code) {
        String key = CAPTCHA_KEY_PREFIX + uuid;
        String cacheCode = redisUtil.get(key);
        if (cacheCode == null) {
            throw new LoginFailException("图形验证码已失效,请点击验证码图片刷新后重试!");
        }
        // 一次性消费：先删再比对，避免同一个爆破脚本反复复用同一个验证码
        redisUtil.delete(key);
        if (!cacheCode.equalsIgnoreCase(code)) {
            throw new LoginFailException("图形验证码错误!");
        }
    }
}
