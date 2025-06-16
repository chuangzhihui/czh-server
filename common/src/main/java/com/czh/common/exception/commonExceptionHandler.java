package com.czh.common.exception;


import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class commonExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JSONResult<Exception> exceptionHandler(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        return JSONResult.error(e.getMessage(),e);
    }
    @ExceptionHandler(value = NoAuthException.class)
    @ResponseBody
    public JSONResult<EmptyVo> noAuthExceptionHandler(NoAuthException e) {
        return JSONResult.permissionDenied();
    }
    @ExceptionHandler(value = NotLoginException.class)
    @ResponseBody
    public JSONResult<EmptyVo> notLoginExceptionHandler(NotLoginException e) {
        return JSONResult.error(e.getMessage());
    }


}
