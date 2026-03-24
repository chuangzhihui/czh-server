package com.czh.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JWTUtil {
    public static String getJWTToken(String userId,String password,String ip,String ua){
        long createTime= (new Date()).getTime();
        String token="";
        token= JWT.create().withAudience(userId)
                .withExpiresAt(DateUtil.addSeconds(
                        new Date(), userId.equals("userGetUploadToken")?600:3600*24*30
                ))
                .withClaim("ip",ip)
                .withClaim("ua",ua)
                .withClaim("createTime",createTime)
                .sign(Algorithm.HMAC256(password));
        return token;
    }
}
