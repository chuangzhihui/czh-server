package com.czh.common.utils;

import com.czh.common.exception.ErrorException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class ValidateUtil {
    /**
     * 校验请求参数是否有误
     * <p>
     * 抛 {@link ErrorException}：约束 message 本身就是给用户看的，
     * 全局处理器会原样返回；抛裸 RuntimeException 会导致走兜底分支、对外只给 traceId。
     *
     * @param result
     */
    public static void checkError(BindingResult result){
        if(result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                throw new ErrorException(error.getDefaultMessage());
            }
        }
    }
}
