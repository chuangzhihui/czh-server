package com.czh.common.config;

import com.czh.common.utils.PWDUtil;

/**
 * 密码编码器
 * <p>
 * 严格遵循 {@link org.springframework.security.crypto.password.PasswordEncoder} 契约：
 * {@code matches} 只返回 true / false，<b>不抛异常</b>；
 * "密码错误"的业务提示由 Spring Security 的 {@code BadCredentialsException}
 * 经登录失败处理器或 Controller 捕获后给出。
 */
public class CZHPasswordEncoder implements org.springframework.security.crypto.password.PasswordEncoder {
    @Override
    public String encode(CharSequence rawPassword) {
        return PWDUtil.createHashPassword(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return PWDUtil.hashpasswordVerify(rawPassword.toString(), encodedPassword);
    }
}
