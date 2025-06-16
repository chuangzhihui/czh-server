package com.czh.common.config;

import com.czh.common.exception.PasswordErrorException;
import com.czh.common.utils.PWDUtil;

public class CZHPasswordEncoder implements org.springframework.security.crypto.password.PasswordEncoder {
    @Override
    public String encode(CharSequence rawPassword) {
        return PWDUtil.createHashPassword(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        boolean match = PWDUtil.hashpasswordVerify(rawPassword.toString(), encodedPassword);
        if(!match)
        {
            throw new PasswordErrorException("密码错误!");
        }
        return true;
    }
}
