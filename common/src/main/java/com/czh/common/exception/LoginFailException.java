package com.czh.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 登录失败异常：携带可直接展示给用户的文案。
 * <p>
 * 用法与 Spring Security 自带的 LockedException / DisabledException 完全一致 ——
 * 由 AuthenticationProvider 抛出，LoginController 统一按 AuthenticationException 捕获后翻译成响应，
 * 让"验证码错误、临近冻结"这类原因也能进入标准的认证失败语义，而不是散落成业务判断。
 * <p>
 * 放在 {@code com.czh.common.exception}：与 ErrorException / NotLoginException 等并列，
 * 统一由 {@code commonExceptionHandler} 兜底，两个模块（admin / api）共用。
 */
public class LoginFailException extends AuthenticationException {

    public LoginFailException(String msg) {
        super(msg);
    }
}
