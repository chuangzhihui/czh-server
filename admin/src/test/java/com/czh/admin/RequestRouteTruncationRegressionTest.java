package com.czh.admin;

import com.czh.common.utils.RequestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * BE-31② 回归：权限路由的 4 段硬截断。
 * <p>
 * 旧实现 {@code int len = Math.min(uris.length, 4)} 只取前 4 段，
 * 5 段及以上的路由会被<b>静默截成父路由</b>，从而继承父路由的权限（权限静默放大）。
 * 当前 48 个端点恰好都 ≤4 段所以没暴露，但作为会被反复 fork 的模板，
 * 下游一旦出现 {@code /admin/user/import/template} 这类 5 段路由就是安全缺陷。
 * <p>
 * 本用例不需要 MySQL / Redis / Spring 容器：{@code getRequestRoute()} 是纯字符串函数，
 * 只依赖 {@code RequestContextHolder}，塞一个 MockHttpServletRequest 即可。
 */
class RequestRouteTruncationRegressionTest {

    /**
     * 用给定 URI 走一遍真实的 getRequestRoute()
     */
    private String routeOf(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return RequestUtil.getRequestRoute();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("BE-31②【核心】：5 段路由不得被截断（修复前返回 /admin/user/import，丢失 template）")
    void fiveSegmentRouteShouldNotBeTruncated() {
        assertEquals("/admin/user/import/template", routeOf("/admin/user/import/template"));
    }

    @Test
    @DisplayName("BE-31②：6 段路由同样完整保留")
    void sixSegmentRouteShouldNotBeTruncated() {
        assertEquals("/admin/a/b/c/d/e", routeOf("/admin/a/b/c/d/e"));
    }

    @Test
    @DisplayName("BE-31②【零回归】：现有 4 段端点行为逐字节不变")
    void fourSegmentRouteShouldKeepSameBehavior() {
        assertEquals("/admin/setting/getUploadConfig", routeOf("/admin/setting/getUploadConfig"));
        assertEquals("/admin/role/roleList", routeOf("/admin/role/roleList"));
        assertEquals("/admin/admin/adminList", routeOf("/admin/admin/adminList"));
        assertEquals("/admin/backup/removeBackUpFile", routeOf("/admin/backup/removeBackUpFile"));
    }

    @Test
    @DisplayName("BE-31②【零回归】：不足 4 段时返回已有部分，且不得数组越界")
    void shortRouteShouldNotThrow() {
        assertEquals("/admin", routeOf("/admin"));
        assertEquals("", routeOf("/"));
        assertEquals("/admin/role", routeOf("/admin/role"));
    }

    @Test
    @DisplayName("BE-31②：无请求上下文时返回空串（原行为保持）")
    void noRequestContextShouldReturnEmpty() {
        assertEquals("", RequestUtil.getRequestRoute());
    }
}
