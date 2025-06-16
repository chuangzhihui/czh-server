package com.czh.api;

import com.czh.common.vo.JSONResult;
import com.czh.service.entity.UploadSet;
import com.czh.service.service.UploadSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @Autowired
    UploadSetService uploadSetService;
    @GetMapping("/ceshi111")
    public JSONResult<UploadSet> ceshi()
    {
        UploadSet admin=uploadSetService.getById(1);
        return JSONResult.success(admin);
    }
}
