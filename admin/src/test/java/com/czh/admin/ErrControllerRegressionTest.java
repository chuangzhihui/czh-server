package com.czh.admin;

import com.czh.admin.baseController.ErrController;
import com.czh.common.exception.ErrorException;
import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BE-29 回归：{@code /error} 出口。
 * <p>
 * 修复前：{@code ErrController} 一律 {@code response.setStatus(404)} +「请求地址不存在」。
 * 由于 {@code JWTAuthFilter} 是 {@code OncePerRequestFilter}（在 DispatcherServlet 之外），
 * 它在 Filter 内抛的 {@code ErrorException} 被 {@code @ControllerAdvice} 漏掉，容器转派 {@code /error}
 * 后被无差别改写成 404 —— <b>伪造上传 token 这类安全事件在监控里被记成爬虫噪音</b>。
 * <p>
 * 修复后：HTTP 状态码层恒 200；按 {@code ERROR_STATUS_CODE} / {@code ERROR_EXCEPTION} 判别真实错误类型。
 */
class ErrControllerRegressionTest {

    private ErrController controller;

    @BeforeEach
    void setUp() {
        controller = new ErrController();
    }

    @Test
    @DisplayName("BE-29：HTTP 状态码层恒 200（不再返回真 404）")
    void shouldAlwaysReturnHttp200() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.error(request, response);

        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("BE-29：无错误属性时按「地址不存在」返回业务码 404")
    void shouldReturn404WhenNoErrorAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JSONResult<EmptyVo> result = controller.error(request, response);

        assertEquals(200, response.getStatus());
        assertEquals(404, result.code);
        assertEquals("请求地址不存在", result.msg);
    }

    @Test
    @DisplayName("BE-29：容器标记的 404 返回业务码 404")
    void shouldReturn404WhenStatusCodeIs404() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);
        MockHttpServletResponse response = new MockHttpServletResponse();

        JSONResult<EmptyVo> result = controller.error(request, response);

        assertEquals(200, response.getStatus());
        assertEquals(404, result.code);
    }

    @Test
    @DisplayName("BE-29：容器标记的 403 返回业务码 403，而不是被抹平成地址不存在")
    void shouldReturn403WhenStatusCodeIs403() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 403);
        MockHttpServletResponse response = new MockHttpServletResponse();

        JSONResult<EmptyVo> result = controller.error(request, response);

        assertEquals(200, response.getStatus());
        assertEquals(403, result.code);
        assertEquals("无权限!", result.msg);
    }

    @Test
    @DisplayName("BE-29【关键】：Filter 抛出的业务异常必须原样透出 message，不得伪装成地址不存在")
    void shouldPassThroughBusinessExceptionMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new ErrorException("token错误或者过期"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        JSONResult<EmptyVo> result = controller.error(request, response);

        assertEquals(200, response.getStatus());
        assertEquals("token错误或者过期", result.msg);
        assertNotEquals("请求地址不存在", result.msg, "安全事件不得被抹成「请求地址不存在」");
    }

    @Test
    @DisplayName("BE-29【关键】：伪造上传 token 被拒时不再是「请求地址不存在」")
    void forgedUploadTokenShouldNotBeReportedAsNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new ErrorException("上传凭证无效或已过期"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        JSONResult<EmptyVo> result = controller.error(request, response);

        assertTrue(result.msg.contains("上传凭证"), "监控里必须能看到这是凭证校验失败");
        assertNotEquals(404, result.code, "鉴权失败不得复用 404");
    }

    @Test
    @DisplayName("BE-29：未知异常只暴露 traceId，不泄露堆栈与类名")
    void unknownExceptionShouldOnlyExposeTraceId() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new IllegalStateException("db connection refused"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        JSONResult<EmptyVo> result = controller.error(request, response);

        assertEquals(200, response.getStatus());
        assertEquals(500, result.code);
        assertTrue(result.msg.contains("traceId="), "应带 traceId 便于反查");
        assertTrue(!result.msg.contains("db connection refused"), "不得把内部异常信息直接回给前端");
    }
}
