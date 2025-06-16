package com.czh.common.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PWDUtil {
    /**
     * 验证密码正确性
     * @param checkPass 输入的密码
     * @param hashPassword hash密文
     * @return
     */
    public static boolean hashpasswordVerify(String checkPass,String hashPassword){
        BCrypt.Result res = BCrypt.verifyer().verify(checkPass.toCharArray(), hashPassword);
        return res.verified;
    }
    //生成密码
    public static String createHashPassword(String password){
        String hash = BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(10, password.toCharArray());
        return hash;

    }
}
