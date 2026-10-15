package com.czh.admin.baseController;


import com.czh.common.exception.ErrorException;
import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;



/**
 * 全局错误出口（BE-29）
 * <p>
 * 为什么必须在这里做"内容判别"：{@code JWTAuthFilter} 是 {@code OncePerRequestFilter}，
 * 在 DispatcherServlet <b>之外</b>执行，它抛出的业务异常 {@code @ControllerAdvice} 抓不到；
 * 全项目又没有任何 {@code ErrorViewResolver} / {@code ErrorPageRegistrar}，容器一律转派 {@code /error}。
 * 原实现无差别返回 404「请求地址不存在」，后果是：伪造上传 token 被拒这类<b>安全事件</b>
 * 在监控里被记成爬虫噪音，P0 漏洞的利用尝试无法被发现与告警。
 * <p>
 * 三条约定：
 * <ol>
 *   <li>HTTP 状态码层恒 200（与 AdminAuthFailedHandler 一致），业务结果看响应体 code；</li>
 *   <li>先读 {@code ERROR_STATUS_CODE} / {@code ERROR_EXCEPTION} 判别真实错误类型，再分类返回；</li>
 *   <li>禁止无差别返回「请求地址不存在」。</li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ErrController implements ErrorController {

    @RequestMapping
    public JSONResult<EmptyVo> error(HttpServletRequest request, HttpServletResponse response){
        // ① HTTP 状态码层恒 200：真 4xx 会让前端 request.ts 的 !res.ok 分支先触发，读不到 body
        response.setStatus(HttpServletResponse.SC_OK);

        // ② 内容判别：容器转派 /error 时会把原始错误放进这两个属性
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorException = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Throwable cause = errorException instanceof Throwable ? (Throwable) errorException : null;

        String uri = request.getRequestURI();

        // 2.1 Filter 抛出的业务异常：原样透出 message（过去会被抹平成"请求地址不存在"）
        if (cause instanceof ErrorException) {
            String msg = cause.getMessage();
            log.warn("业务异常被 /error 兜底 uri={} msg={}", uri, msg);
            return JSONResult.fail(500, msg);
        }

        // 2.2 明确的 403 / 404
        if (statusCode != null) {
            if (statusCode == HttpServletResponse.SC_FORBIDDEN) {
                log.warn("无权限被 /error 兜底 uri={}", uri);
                return JSONResult.permissionDenied();
            }
            if (statusCode == HttpServletResponse.SC_NOT_FOUND) {
                log.info("请求地址不存在 uri={}", uri);
                return JSONResult.notFound();
            }
        }

        // 2.3 其余真实异常：只暴露 traceId，堆栈写日志
        if (cause != null) {
            String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            log.error("系统异常 traceId={} uri={} msg={}", traceId, uri, cause.getMessage(), cause);
            return JSONResult.error("服务异常,traceId=" + traceId);
        }

        // 2.4 兜底：没有任何错误属性，说明就是"地址不存在"
        log.info("请求地址不存在 uri={}", uri);
        return JSONResult.notFound();
    }
}
