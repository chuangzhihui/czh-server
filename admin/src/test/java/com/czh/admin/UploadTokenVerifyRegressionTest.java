package com.czh.admin;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.czh.admin.security.JWTAuthFilter;
import com.czh.common.utils.JWTUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BE-04 回归：上传 token 的验签。
 * <p>
 * 修复前：上传 token 用源码里的字面量常量 {@code "userGetUploadToken"} <b>同时</b>当
 * audience 和 HMAC 签名密钥，而 {@code JWTAuthFilter#checkUploadToken} 全程只
 * {@code JWT.decode(token)}（只解码不验签）。攻击者在本地执行：
 * <pre>
 *   JWT.create().withAudience("userGetUploadToken")
 *       .withClaim("ip", 自己的IP).sign(HMAC256("userGetUploadToken"))
 * </pre>
 * 就能拿到一张合法上传令牌，且 {@code /upload} 在 SecurityConfig 里是 permitAll —— 完整绕过链。
 * <p>
 * 本组用例是这条 P0 缺陷的验收标准：<b>用旧常量密钥伪造的 token 必须被拒</b>。
 */
class UploadTokenVerifyRegressionTest {

    /** 修复前被当作密钥使用的公开常量 */
    private static final String OLD_HARDCODED_SECRET = "userGetUploadToken";

    /** 修复后由配置项 czh.upload-token.secret 注入的独立密钥 */
    private static final String REAL_SECRET = "czh-upload-token-secret-for-test-0001";

    private static final String CLIENT_IP = "203.0.113.9";

    private JWTAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JWTAuthFilter();
        ReflectionTestUtils.setField(filter, "uploadTokenSecret", REAL_SECRET);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("BE-04：用旧的硬编码常量密钥伪造的 token 必须验签失败")
    void forgedTokenWithOldConstantSecretShouldBeRejected() {
        String forged = JWT.create()
                .withAudience(OLD_HARDCODED_SECRET)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .withClaim("ip", CLIENT_IP)
                .sign(Algorithm.HMAC256(OLD_HARDCODED_SECRET));

        assertFalse(JWTUtil.isValid(forged, REAL_SECRET), "用公开常量签出的 token 不得通过验签");

        MockHttpServletResponse response = doUpload(forged, CLIENT_IP);
        assertRejected(response);
    }

    @Test
    @DisplayName("BE-04：伪造 token 时过滤器链不得继续（请求必须被拦下）")
    void forgedTokenShouldNotContinueFilterChain() {
        String forged = JWT.create()
                .withAudience(OLD_HARDCODED_SECRET)
                .withClaim("ip", CLIENT_IP)
                .sign(Algorithm.HMAC256(OLD_HARDCODED_SECRET));

        MockHttpServletResponse response = doUpload(forged, CLIENT_IP);
        assertRejected(response);
    }

    @Test
    @DisplayName("BE-04：用正确密钥签发且 IP 匹配的 token 放行（不误伤正常上传）")
    void validTokenShouldPass() {
        String token = JWTUtil.getJWTToken(JWTUtil.UPLOAD_TOKEN_AUDIENCE, REAL_SECRET, CLIENT_IP);

        assertTrue(JWTUtil.isValid(token, REAL_SECRET));

        MockHttpServletRequest request = buildUploadRequest(token, CLIENT_IP);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertDoesNotThrowFilter(() -> filter.doFilter(request, response, chain));

        assertEquals(200, response.getStatus());
        // 放行时过滤器链必须继续执行
        assertNotNull(chain.getRequest(), "合法 token 应继续过滤器链");
        assertTrue(bodyOf(response).isEmpty(), "放行时不应写出响应体");
    }

    @Test
    @DisplayName("BE-04：token 过期必须被拒")
    void expiredTokenShouldBeRejected() {
        String expired = JWTUtil.getJWTToken(JWTUtil.UPLOAD_TOKEN_AUDIENCE, REAL_SECRET, CLIENT_IP, -60);

        assertFalse(JWTUtil.isValid(expired, REAL_SECRET), "过期 token 不得通过验签");

        MockHttpServletResponse response = doUpload(expired, CLIENT_IP);
        assertRejected(response);
    }

    @Test
    @DisplayName("BE-04：IP 与 token 中绑定 IP 不一致时被拒")
    void ipMismatchShouldBeRejected() {
        String token = JWTUtil.getJWTToken(JWTUtil.UPLOAD_TOKEN_AUDIENCE, REAL_SECRET, CLIENT_IP);

        MockHttpServletResponse response = doUpload(token, "198.51.100.7");
        assertRejected(response);
    }

    @Test
    @DisplayName("BE-04：缺少 token 时被拒，且返回的是 401 而不是「请求地址不存在」")
    void missingTokenShouldBeRejected() {
        MockHttpServletResponse response = doUpload(null, CLIENT_IP);
        assertRejected(response);
        assertTrue(bodyOf(response).contains("401"));
    }

    @Test
    @DisplayName("BE-04：篡改 payload（换个 audience）后验签失败")
    void tamperedAudienceShouldBeRejected() {
        String token = JWTUtil.getJWTToken("admin", REAL_SECRET, CLIENT_IP);
        // 密钥正确但 audience 不是上传令牌
        MockHttpServletResponse response = doUpload(token, CLIENT_IP);
        assertRejected(response);
    }

    @Test
    @DisplayName("BE-04：UploadSetServiceImpl 启动期必须拒绝空密钥（配置占位未填则失败）")
    void blankSecretShouldBeRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            com.czh.common.utils.JWTUtil.getJWTToken(JWTUtil.UPLOAD_TOKEN_AUDIENCE, "", CLIENT_IP);
        });
    }

    // ==================== 辅助 ====================

    private MockHttpServletResponse doUpload(String token, String clientIp) {
        MockHttpServletRequest request = buildUploadRequest(token, clientIp);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertDoesNotThrowFilter(() -> filter.doFilter(request, response, chain));

        // 被拒时过滤器链不得继续
        assertNull(chain.getRequest(), "校验失败时不得继续过滤器链");
        return response;
    }

    private MockHttpServletRequest buildUploadRequest(String token, String clientIp) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/upload");
        request.setRequestURI("/upload");
        request.addHeader("X-Real-IP", clientIp);
        if (token != null) {
            request.addHeader("token", token);
        }
        return request;
    }

    /**
     * 读取响应体（MockHttpServletResponse 的 getter 声明了受检异常，此处包一层）
     */
    private String bodyOf(MockHttpServletResponse response) {
        try {
            return response.getContentAsString(StandardCharsets.UTF_8);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private void assertRejected(MockHttpServletResponse response) {
        // BE-29：HTTP 状态码层恒 200，业务结果看响应体 code
        assertEquals(200, response.getStatus(), "HTTP 状态码层必须恒 200");
        String body = bodyOf(response);
        assertTrue(body.contains("\"code\":401"), "上传凭证失败应返回业务码 401，实际: " + body);
        assertTrue(body.contains("上传凭证"), "应给出可读的凭证错误文案，实际: " + body);
    }

    private void assertDoesNotThrowFilter(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new AssertionError("Filter 不得向外抛异常（BE-29 治本项）: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
