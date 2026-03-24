package com.czh.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.common.exception.ErrorException;
import com.czh.common.utils.JWTUtil;
import com.czh.common.utils.RequestUtil;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import com.czh.service.dao.UploadSetDao;
import com.czh.service.entity.UploadSet;
import com.czh.service.service.UploadSetService;
import com.czh.service.vo.admin.GetUploadTokenVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


/**
 * 文件上传配置(UploadSet)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Service
public class UploadSetServiceImpl extends ServiceImpl<UploadSetDao,UploadSet> implements UploadSetService {
    @Autowired
    @Lazy
    UploadSetService uploadSetService;

    @Override
    public GetUploadTokenVo getUploadToken(HttpServletRequest request) {
        UploadSet set=uploadSetService.getById(1);
        if(set==null || set.getLocal()==null || set.getLocal().isEmpty() || set.getVisible().equals(0)){
            throw new ErrorException("未配置上传信息!");
        }
        LocalUploadConfig localUploadConfig= JSON.toJavaObject(JSONObject.parseObject(set.getLocal()),LocalUploadConfig.class);
        GetUploadTokenVo vo = new GetUploadTokenVo();
        String token = JWTUtil.getJWTToken("userGetUploadToken","userGetUploadToken", RequestUtil.getIp(),request.getHeader("user-agent"));
        vo.setToken(token);
        vo.setHost(localUploadConfig.getHost());
        return vo;
    }
}
