package com.czh.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Claim;

import java.util.Date;
import java.util.List;

public class JWTUtil {
    /**
     * 上传专用令牌的固定 audience。
     * <p>
     * 注意：它<b>只是令牌的用途标识，不再是签名密钥</b>。签名密钥改为独立配置项
     * {@code czh.upload-token.secret}（见 BE-04）——密钥等于公开常量时，任何人都能自己签令牌。
     */
    public static final String UPLOAD_TOKEN_AUDIENCE = "userGetUploadToken";

    /** 上传令牌有效期（秒）：短期有效，泄露窗口尽量小 */
    private static final int UPLOAD_TOKEN_TTL_SECONDS = 600;

    /** 管理员令牌有效期（秒）：30 天（沿用既有行为，BE-06 会另行收敛） */
    private static final int ADMIN_TOKEN_TTL_SECONDS = 3600 * 24 * 30;

    private JWTUtil() {
        // 工具类禁止实例化
    }

    /**
     * 签发 JWT
     *
     * @param audience 令牌用途/用户标识（上传令牌固定为 {@link #UPLOAD_TOKEN_AUDIENCE}）
     * @param secret   HMAC 签名密钥，<b>必须是独立配置的密钥，禁止传业务数据或公开常量</b>
     * @param ip       绑定 IP
     * @return JWT 字符串
     */
    public static String getJWTToken(String audience, String secret, String ip) {
        int ttl = UPLOAD_TOKEN_AUDIENCE.equals(audience) ? UPLOAD_TOKEN_TTL_SECONDS : ADMIN_TOKEN_TTL_SECONDS;
        return getJWTToken(audience, secret, ip, ttl);
    }

    /**
     * 签发 JWT（可指定有效期）
     *
     * @param audience   令牌用途/用户标识
     * @param secret     HMAC 签名密钥
     * @param ip         绑定 IP
     * @param ttlSeconds 有效期（秒）
     * @return JWT 字符串
     */
    public static String getJWTToken(String audience, String secret, String ip, int ttlSeconds) {
        requireSecret(secret);
        return JWT.create()
                .withAudience(audience)
                .withExpiresAt(DateUtil.addSeconds(new Date(), ttlSeconds))
                .withClaim("ip", ip)
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 真正验签：签名不对 / 已过期 / 结构非法都会抛 {@link JWTVerificationException}。
     * <p>
     * 与 {@code JWT.decode(token)} 的区别：decode 只是 Base64 解码，<b>不校验签名</b>，
     * 攻击者可以随意篡改 payload 后重新编码。任何鉴权判定都必须走本方法。
     *
     * @param token  待校验令牌
     * @param secret HMAC 签名密钥
     * @return 验签通过后的解码结果
     * @throws JWTVerificationException 验签失败或令牌过期
     */
    public static DecodedJWT verify(String token, String secret) throws JWTVerificationException {
        requireSecret(secret);
        if (token == null || token.isEmpty()) {
            throw new JWTVerificationException("token 为空");
        }
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        return verifier.verify(token);
    }

    /**
     * 验签并返回是否通过（不抛异常，便于判定分支使用）
     *
     * @param token  待校验令牌
     * @param secret HMAC 签名密钥
     * @return 通过返回 true
     */
    public static boolean isValid(String token, String secret) {
        try {
            return verify(token, secret) != null;
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 验签后读取 audience（第一个）
     *
     * @param token  待校验令牌
     * @param secret HMAC 签名密钥
     * @return audience；不存在返回空串
     * @throws JWTVerificationException 验签失败或令牌过期
     */
    public static String getAudience(String token, String secret) throws JWTVerificationException {
        DecodedJWT jwt = verify(token, secret);
        List<String> audience = jwt.getAudience();
        if (audience == null || audience.isEmpty()) {
            return "";
        }
        return audience.get(0);
    }

    /**
     * 验签后读取自定义 claim
     *
     * @param token  待校验令牌
     * @param secret HMAC 签名密钥
     * @param name   claim 名称
     * @return claim 值；不存在返回 null
     * @throws JWTVerificationException 验签失败或令牌过期
     */
    public static String getStringClaim(String token, String secret, String name) throws JWTVerificationException {
        DecodedJWT jwt = verify(token, secret);
        Claim claim = jwt.getClaim(name);
        if (claim == null || claim.isNull()) {
            return null;
        }
        return claim.asString();
    }

    private static void requireSecret(String secret) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("JWT 签名密钥未配置，拒绝签发/校验令牌");
        }
    }
}
