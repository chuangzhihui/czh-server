package com.czh.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.czh.common.utils.aliOssUtil.AliOssUtil;
import com.czh.common.utils.aliOssUtil.entity.AliOssEntity;
import com.czh.common.utils.qiniuUtil.QiniuUtil;
import com.czh.common.utils.qiniuUtil.entity.QiniuEntity;
import com.czh.common.utils.tosUtil.TosUtil;
import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.czh.common.utils.txCosUtil.TxCosUtil;
import com.czh.common.utils.txCosUtil.entity.TxCosEntity;
import com.czh.service.entity.AdminActionLog;
import com.czh.service.entity.UploadFiles;
import com.czh.service.entity.UploadSet;
import com.czh.service.service.AdminActionLogService;
import com.czh.service.service.AsyncService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Service
@Log4j2
public class AsyncServiceImpl implements AsyncService {
    @Autowired
    AdminActionLogService adminActionLogService;
    @Override
    @Async
    public void ceshiAsync() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("ceshiAsync");
    }

    @Override
    @Async
    public void addAdminLog(AdminActionLog adminActionLog) {
        adminActionLogService.saveAdminActionLog(adminActionLog);
    }

    @Override
    @Async
    public void removeFile(UploadFiles file, UploadSet set) {
        int domain=file.getDomain();
        String key=file.getKey();
        if (domain == 1) {
            //七牛文件
            QiniuEntity qiniuEntity = JSON.toJavaObject(JSONObject.parseObject(set.getQiniu()), QiniuEntity.class);
            QiniuUtil qiniu = new QiniuUtil(qiniuEntity);
            qiniu.delFile(key);
        } else if (domain == 2) {
            //阿里OSS
            AliOssEntity aliOssEntity = JSON.toJavaObject(JSONObject.parseObject(set.getAlioss()), AliOssEntity.class);
            AliOssUtil aliOSS = new AliOssUtil(aliOssEntity);
            aliOSS.delFile(key);
        } else if (domain == 3) {
            //腾讯cos
            TxCosEntity txCosEntity = JSON.toJavaObject(JSONObject.parseObject(set.getTxcos()), TxCosEntity.class);
            TxCosUtil txCos = new TxCosUtil(txCosEntity);
            txCos.delFile(key);
        }else if (domain == 4) {
            //删除本地目录文件
            File f = new File(file.getKey());
            if (f.exists()) {
                f.delete();
            }
        } else if (domain == 5) {
            //火山云
            TosEntity tosEntity = JSON.toJavaObject(JSONObject.parseObject(set.getTos()), TosEntity.class);
            TosUtil tosUtil = new TosUtil(tosEntity);
            tosUtil.delFile(key);
        }
    }
}
