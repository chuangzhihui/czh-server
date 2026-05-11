package com.czh.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JWTUtil {
    /**
     *
     * @param userId
     * @param password
     * @param ip
     * @param role 身份 0上传token 1后台管理员 2普通用户 3招聘企业用户
     * @return
     */
    public static String getJWTToken(String userId,String password,String ip,Integer role){
        String token="";
        token= JWT.create().withAudience(userId)
                .withExpiresAt(DateUtil.addSeconds(
                        new Date(), userId.equals("userGetUploadToken")?600:3600*24*30
                ))
                .withClaim("ip",ip)
                .withClaim("role",role)
                .sign(Algorithm.HMAC256(password));
        return token;
    }
}
