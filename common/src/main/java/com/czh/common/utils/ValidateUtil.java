package com.czh.common.utils;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class ValidateUtil {
    /**
     * 校验请求参数是否有误
     * @param result
     */
    public static void checkError(BindingResult result){
        if(result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                throw  new RuntimeException(error.getDefaultMessage());
            }
        }
    }
}
