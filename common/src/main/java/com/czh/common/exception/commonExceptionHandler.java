package com.czh.common.exception;


import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * 全局异常处理器
 * <p>
 * 原则：<b>业务异常返回可读文案，未知异常只返回 traceId</b>，
 * 绝不把异常对象、堆栈、类名等内部信息序列化进响应体。
 */
@Slf4j
@ControllerAdvice
public class commonExceptionHandler {

    /**
     * 业务异常：文案本身就是给用户看的（如"未配置上传信息!"），原样返回，只记 warn
     */
    @ExceptionHandler(value = ErrorException.class)
    @ResponseBody
    public JSONResult<EmptyVo> errorExceptionHandler(HttpServletRequest request, ErrorException e) {
        log.warn("业务异常 uri={} msg={}", request.getRequestURI(), e.getMessage());
        return JSONResult.error(e.getMessage());
    }

    /**
     * 密码错误异常：登录失败提示，文案对用户可见，不记录堆栈
     */
    @ExceptionHandler(value = PasswordErrorException.class)
    @ResponseBody
    public JSONResult<EmptyVo> passwordErrorExceptionHandler(HttpServletRequest request, PasswordErrorException e) {
        log.warn("密码错误 uri={} msg={}", request.getRequestURI(), e.getMessage());
        return JSONResult.error(e.getMessage());
    }

    /**
     * 未知异常：对外只暴露 traceId，完整堆栈写日志（便于用 traceId 反查）
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JSONResult<EmptyVo> exceptionHandler(HttpServletRequest request, Exception e) {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        log.error("系统异常 traceId={} uri={} msg={}", traceId, request.getRequestURI(), e.getMessage(), e);
        return JSONResult.error("服务异常,traceId=" + traceId);
    }

    @ExceptionHandler(value = NoAuthException.class)
    @ResponseBody
    public JSONResult<EmptyVo> noAuthExceptionHandler(NoAuthException e) {
        return JSONResult.permissionDenied();
    }

    @ExceptionHandler(value = NotLoginException.class)
    @ResponseBody
    public JSONResult<EmptyVo> notLoginExceptionHandler(NotLoginException e) {
        return JSONResult.error(e.getMessage());
    }


}
