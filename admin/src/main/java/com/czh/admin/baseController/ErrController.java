package com.czh.admin.baseController;


import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ErrController implements ErrorController {
    @RequestMapping
    public JSONResult<EmptyVo> error(HttpServletRequest request, HttpServletResponse response){
        response.setStatus(404);
        return JSONResult.error("请求地址不存在");
    }
}
