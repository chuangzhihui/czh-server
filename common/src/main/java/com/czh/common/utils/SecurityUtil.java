package com.czh.common.utils;

import cn.hutool.core.util.ObjectUtil;
import com.czh.common.exception.ErrorException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static <T> T getLoginInfo(Class<T> classZZ)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        T loginUserVo = (T) authentication.getPrincipal();
        if (ObjectUtil.isNull(loginUserVo)) {
            throw new ErrorException("登录信息已过期，请重新登录");
        }
        return loginUserVo;
    }
}
