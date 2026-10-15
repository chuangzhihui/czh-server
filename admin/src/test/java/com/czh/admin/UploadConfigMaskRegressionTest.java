package com.czh.admin;

import com.czh.admin.baseController.setting.SettingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BE-05 回归：云存储密钥脱敏。
 * <p>
 * 修复前：{@code getUploadConfig} 把 4 家云存储的 {@code secretKey} / {@code sk} / {@code ak}
 * <b>原样序列化</b>进 HTTP 响应体，任何有该权限的账号可一次性拿到全部对象存储主密钥。
 * 修复后：读取时只回「前 4 位 + ****」，保存时忽略掩码值与空值（否则前端一"打开就保存"
 * 会把 **** 写回库里覆盖真值）。
 */
class UploadConfigMaskRegressionTest {

    private SettingController controller;

    @BeforeEach
    void setUp() {
        controller = new SettingController();
    }

    @Test
    @DisplayName("BE-05：密钥被掩码，只保留前 4 位")
    void shouldMaskSecretKeepingPrefix() {
        String masked = invokeMask("wJalrXUtnFEMI-K7MDENG-bx");
        assertTrue(masked.endsWith("****"));
        assertEquals("wJal****", masked);
        assertFalse(masked.contains("XUtnFEMI"), "掩码后不得残留密钥主体");
    }

    @Test
    @DisplayName("BE-05：超短密钥整段掩码，不泄露长度信息")
    void shouldFullyMaskShortSecret() {
        assertEquals("****", invokeMask("abc"));
        assertEquals("****", invokeMask("abcd"));
    }

    @Test
    @DisplayName("BE-05：null / 空值原样返回，不臆造内容")
    void shouldKeepNullOrEmpty() {
        assertNull(invokeMask(null));
        assertEquals("", invokeMask(""));
    }

    @Test
    @DisplayName("BE-05：掩码值与空值在保存时必须被识别为「未改动」")
    void shouldTreatMaskedOrEmptyAsUnchanged() {
        assertTrue(invokeIsMaskedOrEmpty("wJal****"));
        assertTrue(invokeIsMaskedOrEmpty(null));
        assertTrue(invokeIsMaskedOrEmpty(""));
        assertTrue(invokeIsMaskedOrEmpty("   "));
        assertFalse(invokeIsMaskedOrEmpty("wJalrXUtnFEMI-K7MDENG-bx"), "真实新密钥必须被视为已改动");
    }

    @Test
    @DisplayName("BE-05：掩码后的值不会再被判定为合法密钥（避免掩码回写覆盖真值）")
    void maskedValueShouldNeverLookLikeRealSecret() {
        String masked = invokeMask("wJalrXUtnFEMI-K7MDENG-bx");
        assertTrue(invokeIsMaskedOrEmpty(masked), "掩码值必须能被保存侧识别并忽略");
        assertFalse(masked.equals("wJalrXUtnFEMI-K7MDENG-bx"));
    }

    @Test
    @DisplayName("BE-05：阿里云/腾讯云的 ak / sk 同样走掩码")
    void shouldMaskAkAndSk() {
        assertEquals("LTAI****", invokeMask("LTAI5tQdFkMhVbNk"));
        assertEquals("abcd****", invokeMask("abcdefghijklmnop"));
    }

    private String invokeMask(String value) {
        return ReflectionTestUtils.invokeMethod(controller, "maskSecret", value);
    }

    private boolean invokeIsMaskedOrEmpty(String value) {
        Boolean result = ReflectionTestUtils.invokeMethod(controller, "isMaskedOrEmpty", value);
        return Boolean.TRUE.equals(result);
    }
}
