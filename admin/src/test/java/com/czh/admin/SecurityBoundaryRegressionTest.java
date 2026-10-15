package com.czh.admin;

import com.czh.admin.security.JWTAuthFilter;
import com.czh.common.dto.PageDto;
import com.czh.common.utils.JWTUtil;
import com.czh.common.utils.OrderByUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安全链路的<b>边界与错误路径</b>补充。
 * <p>
 * 已有的回归用例覆盖了"报告里点名的那条 payload"，但攻击者不会照抄 payload。
 * 本组用例专门打边界：空值、超长、换行/制表符、Unicode 同形字、巨型入参、并发读、
 * 以及"验签通过但结构异常"这类夹缝场景 —— 目的是确认白名单是<b>按规则收口</b>，
 * 而不是按"黑名单里有没有这一条"收口。
 */
class SecurityBoundaryRegressionTest {

    private static final String REAL_SECRET = "qa-upload-token-secret-0123456789";

    // ==================== BE-03：orderBy 边界 ====================

    @ParameterizedTest(name = "orderBy=[{0}]")
    @ValueSource(strings = {
            "asc", "DESC", "Asc", "  desc  ",
            "adminId desc", "adminId   asc", "_id DESC",
            "a1_b2 asc", "createTime desc"
    })
    @DisplayName("BE-03 边界：合法排序形态一律保留（不误伤）")
    void legalOrderByMustBePreserved(String raw) {
        String safe = OrderByUtil.sanitize(raw);
        assertFalse(safe.isEmpty(), "合法排序被误杀: " + raw);
        assertTrue(OrderByUtil.isValid(safe), "归一化结果必须仍合法: " + safe);
    }

    @ParameterizedTest(name = "orderBy=[{0}]")
    @ValueSource(strings = {
            // SQL 元字符
            "adminId desc;drop table czh_admin",
            "adminId' desc", "adminId\" desc", "adminId` desc",
            "adminId desc/*x*/", "adminId--desc", "adminId#desc",
            // 分隔符 / 括号 / 子查询
            "asc,1=(select 1 from (select sleep(5))a)",
            "(select 1)", "adminId,(select 1)", "adminId desc,(select 1)",
            // 换行与制表符绕过（试图让正则的 ^$ 失效）
            "adminId desc\n1=1", "adminId\tdesc\nOR 1=1",
            "adminId desc\r\n; shutdown",
            // 注释绕过
            "adminId/**/desc", "adminId desc #", "adminId desc -- x",
            // Unicode / 全角 / 零宽字符
            "adminId　desc", "adminId​ desc", "ａdminId desc",
            // 超长
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa desc",
            // 多段
            "adminId desc asc", "adminId asc desc extra",
            // 数字开头字段名
            "1admin desc", "0 desc"
    })
    @DisplayName("BE-03 边界：注入/畸形排序串一律归空（含换行、Unicode、超长）")
    void illegalOrderByMustBeDropped(String raw) {
        String safe = OrderByUtil.sanitize(raw);
        assertTrue(safe.isEmpty(),
                "非法排序串必须被丢弃，实际归一化成: [" + safe + "]，原始输入: [" + raw + "]");
    }

    /**
     * 前后空白与内联换行属于<b>可安全归一化</b>的输入：先 trim、再按 \s+ 折叠，
     * 归一化结果必须落在白名单内，而不是"整条丢弃"。
     * 这两者都是安全的（都不可能带进 SQL 元字符），关键是结果必须仍被白名单收口。
     */
    @ParameterizedTest(name = "orderBy=[{0}]")
    @ValueSource(strings = {" adminId desc", "adminId desc ", "adminId\nasc", "adminId\tdesc", "  adminId   asc  "})
    @DisplayName("BE-03 边界：含前后空白/内联换行的输入被归一化后仍必须落在白名单内")
    void whitespaceInputMustBeNormalizedToSafeValue(String raw) {
        String safe = OrderByUtil.sanitize(raw);
        assertTrue(OrderByUtil.isValid(safe),
                "归一化结果必须落在白名单内，实际: [" + safe + "]，原始输入: [" + raw + "]");
        assertFalse(safe.matches(".*[\\s]+$"), "归一化结果不得残留空白: [" + safe + "]");
        assertFalse(safe.contains("\n") || safe.contains("\t"),
                "归一化结果不得残留换行/制表符: [" + safe + "]");
    }

    @Test
    @DisplayName("BE-03 边界：字段名长度上限 64（第 65 位必须被拒）")
    void fieldLengthBoundary() {
        String len64 = "a".repeat(64);
        String len65 = "a".repeat(65);
        assertTrue(OrderByUtil.isValidField(len64), "64 位字段名应合法");
        assertFalse(OrderByUtil.isValidField(len65), "65 位字段名应被拒");
        assertEquals("", OrderByUtil.build(len65, "asc"));
    }

    @Test
    @DisplayName("BE-03 边界：null / 空 / 纯空白一律归空且不抛异常")
    void nullAndBlankMustBeSafe() {
        assertEquals("", OrderByUtil.sanitize(null));
        assertEquals("", OrderByUtil.sanitize(""));
        assertEquals("", OrderByUtil.sanitize("   "));
        assertEquals("", OrderByUtil.sanitize("\n\t\r"));
    }

    @Test
    @DisplayName("BE-03：PageDto 的 getOrderBy 是幂等的（反复读不放大、不变脏）")
    void pageDtoGetterMustBeIdempotent() {
        PageDto dto = new PageDto();
        dto.setOrderBy("adminId desc");
        String first = dto.getOrderBy();
        String second = dto.getOrderBy();
        assertEquals(first, second);
        assertEquals("adminId desc", first);
        // 读操作不得回写原始字段（避免"读一次就永久改了入参"的副作用）
        assertEquals("adminId desc", dto.getOrderByRaw());
    }

    @Test
    @DisplayName("BE-03：超大 size 之外的超长 orderBy（1 万字符）不得导致异常或放行")
    void veryLongOrderByMustBeDropped() {
        String huge = "a".repeat(10_000) + " asc";
        long start = System.currentTimeMillis();
        String safe = OrderByUtil.sanitize(huge);
        long cost = System.currentTimeMillis() - start;
        assertEquals("", safe);
        assertTrue(cost < 1000, "超长输入不得造成正则灾难性回溯（耗时 " + cost + "ms）");
    }

    // ==================== BE-04：上传 token 边界 ====================

    @Test
    @DisplayName("BE-04 边界：token 为空串 / 字面量 false 一律被拒")
    void blankTokenMustBeRejected() throws Exception {
        assertRejectedWith401(doUpload("", "203.0.113.9", REAL_SECRET));
        assertRejectedWith401(doUpload("false", "203.0.113.9", REAL_SECRET));
        assertRejectedWith401(doUpload(null, "203.0.113.9", REAL_SECRET));
        assertRejectedWith401(doUpload("   ", "203.0.113.9", REAL_SECRET));
    }

    @Test
    @DisplayName("BE-04 边界：结构畸形但签名正确的 token 也不得放行")
    void malformedButSignedTokenMustBeRejected() throws Exception {
        // 签名正确、 audience 正确，但故意不带 ip claim
        String noIp = com.auth0.jwt.JWT.create()
                .withAudience(JWTUtil.UPLOAD_TOKEN_AUDIENCE)
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(REAL_SECRET));
        assertRejectedWith401(doUpload(noIp, "203.0.113.9", REAL_SECRET));

        // 签名正确、audience 正确、ip 正确，但已过期的极端边界（-1 秒）
        String justExpired = JWTUtil.getJWTToken(JWTUtil.UPLOAD_TOKEN_AUDIENCE, REAL_SECRET, "203.0.113.9", -1);
        assertRejectedWith401(doUpload(justExpired, "203.0.113.9", REAL_SECRET));
    }

    @Test
    @DisplayName("BE-04：完全不传 token 请求头时也不得放行")
    void absentTokenHeaderMustBeRejected() throws Exception {
        assertRejectedWith401(doUpload(null, "203.0.113.9", REAL_SECRET));
    }

    @Test
    @DisplayName("BE-04：上传令牌必须是短期（<=600 秒），不得沿用管理员令牌的 30 天")
    void uploadTokenMustBeShortLived() {
        String token = JWTUtil.getJWTToken(JWTUtil.UPLOAD_TOKEN_AUDIENCE, REAL_SECRET, "203.0.113.9");
        long exp = com.auth0.jwt.JWT.decode(token).getExpiresAt().getTime();
        long ttlSeconds = (exp - System.currentTimeMillis()) / 1000;
        assertTrue(ttlSeconds <= 600, "上传令牌有效期必须 <=600 秒，实际 " + ttlSeconds + " 秒");
        assertTrue(ttlSeconds > 0, "上传令牌必须是未过期的");
    }

    @Test
    @DisplayName("BE-04：密钥不一致时（签发用 A、校验用 B）必须被拒")
    void secretMismatchMustBeRejected() throws Exception {
        String token = JWTUtil.getJWTToken(JWTUtil.UPLOAD_TOKEN_AUDIENCE, "secret-aaaaaaaaaaaaaa", "203.0.113.9");
        assertRejectedWith401(doUpload(token, "203.0.113.9", "secret-bbbbbbbbbbbbbb"));
    }

    // ==================== 辅助 ====================

    private MockHttpServletResponse doUpload(String token, String clientIp, String filterSecret) throws Exception {
        JWTAuthFilter filter = new JWTAuthFilter();
        ReflectionTestUtils.setField(filter, "uploadTokenSecret", filterSecret);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/upload");
        request.setRequestURI("/upload");
        request.addHeader("X-Real-IP", clientIp);
        if (token != null) {
            request.addHeader("token", token);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        try {
            filter.doFilter(request, response, chain);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
        return response;
    }

    private void assertRejectedWith401(MockHttpServletResponse response) throws Exception {
        assertEquals(200, response.getStatus(), "HTTP 状态码层恒 200（BE-29）");
        String body = response.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(body.contains("\"code\":401"), "应返回业务码 401，实际: " + body);
    }
}
