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
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


/**
 * 文件上传配置(UploadSet)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Slf4j
@Service
public class UploadSetServiceImpl extends ServiceImpl<UploadSetDao,UploadSet> implements UploadSetService {

    /** 上传令牌签名密钥的**最小**长度，防止被配成弱口令 */
    private static final int MIN_SECRET_LENGTH = 16;

    /**
     * 上传令牌的 HMAC 签名密钥（BE-04）。
     * <p>
     * 原实现把字面量常量 {@code "userGetUploadToken"} 同时当 audience 和签名密钥，
     * 而该常量写在源码里、随 jar 分发，等于把密钥公开 —— 任何人都能自己签一张上传令牌。
     * 现改为独立配置项 {@code czh.upload-token.secret}，默认值配置在 application.yaml。
     */
    @Value("${czh.upload-token.secret}")
    private String uploadTokenSecret;

    @Autowired
    @Lazy
    UploadSetService uploadSetService;

    /**
     * 启动期自检：密钥缺失或强度不足直接失败，避免"配了等于没配"的静默降级。
     */
    @PostConstruct
    public void checkUploadTokenSecret() {
        if (uploadTokenSecret == null || uploadTokenSecret.isBlank()) {
            throw new IllegalStateException("配置项 czh.upload-token.secret 未设置：上传令牌签名密钥禁止留空");
        }
        if (uploadTokenSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "配置项 czh.upload-token.secret 强度不足：长度需 >= " + MIN_SECRET_LENGTH + " 位");
        }
        log.info("上传令牌签名密钥已加载（长度 {}，不打印内容）", uploadTokenSecret.length());
    }

    @Override
    public GetUploadTokenVo getUploadToken() {
        UploadSet set=uploadSetService.getById(1);
        if(set==null || set.getLocal()==null || set.getLocal().isEmpty() || set.getVisible().equals(0)){
            throw new ErrorException("未配置上传信息!");
        }
        LocalUploadConfig localUploadConfig= JSON.toJavaObject(JSONObject.parseObject(set.getLocal()),LocalUploadConfig.class);
        GetUploadTokenVo vo = new GetUploadTokenVo();
        String token = JWTUtil.getJWTToken(JWTUtil.UPLOAD_TOKEN_AUDIENCE, uploadTokenSecret, RequestUtil.getIp());
        vo.setToken(token);
        vo.setHost(localUploadConfig.getHost());
        return vo;
    }
}
