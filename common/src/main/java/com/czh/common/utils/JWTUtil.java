package com.czh.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JWTUtil {
    public static String getJWTToken(String userId,String password,String ip){
        String token="";
        token= JWT.create().withAudience(userId)
                .withExpiresAt(DateUtil.addSeconds(new Date(),3600*24*30))
                .withClaim("ip",ip)
                .sign(Algorithm.HMAC256(password));
        return token;
    }
}
