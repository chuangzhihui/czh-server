package com.czh.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.RedisUtil;
import com.czh.common.utils.StringUtil;
import com.czh.common.utils.aliOssUtil.AliOssUtil;
import com.czh.common.utils.aliOssUtil.entity.AliOssEntity;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import com.czh.common.utils.qiniuUtil.QiniuUtil;
import com.czh.common.utils.qiniuUtil.entity.QiniuEntity;
import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.czh.common.utils.txCosUtil.TxCosUtil;
import com.czh.common.utils.txCosUtil.entity.TxCosEntity;
import com.czh.service.dao.UploadSetDao;
import com.czh.service.entity.UploadSet;
import com.czh.service.service.UploadSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


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
    RedisUtil redisUtil;
    @Override
    public Map<String, Object> getUploadToken() {
        UploadSet set=getById(1);
        if(set==null){
            throw new Error("未配置上传信息");
        }
        Map<String,Object> json=new HashMap<>();
        if(set.getVisible()==1){
            //七牛
            QiniuEntity qiniuEntity= JSON.toJavaObject(JSONObject.parseObject(set.getQiniu()),QiniuEntity.class);
            QiniuUtil qiniu=new QiniuUtil(qiniuEntity);
            json.put("token",qiniu.getToken());
            json.put("domain",qiniuEntity.getDomain());
            json.put("endpoint",qiniuEntity.getEndpoint());
        }else if(set.getVisible()==2){
            //阿里oss
            AliOssEntity aliOssEntity=JSON.toJavaObject(JSONObject.parseObject(set.getAlioss()), AliOssEntity.class);
            AliOssUtil aliOSS=new AliOssUtil(aliOssEntity);
            json=aliOSS.getToken();
        }else if(set.getVisible()==3){
            //腾讯cos
            TxCosEntity txCosEntity=JSON.toJavaObject(JSONObject.parseObject(set.getTxcos()),TxCosEntity.class);
            TxCosUtil txCos=new TxCosUtil(txCosEntity);
            json=txCos.getTempKey();
        }else if(set.getVisible()==4){

            //本地上传
            LocalUploadConfig localUploadConfig= JSON.toJavaObject(JSONObject.parseObject(set.getLocal()),LocalUploadConfig.class);
            String uptoken= StringUtil.getRadomStr(32);
            redisUtil.set(uptoken,"1");
            redisUtil.expireAt(uptoken, DateUtil.addSeconds(DateUtil.getDaDate(),3600*24));
            json.put("token",uptoken);
            json.put("uploadUrl",localUploadConfig.getHost());
            json.put("domain",localUploadConfig.getDomain());
        }else if(set.getVisible().equals(5)){
            //火山云-这里啥也不返回
            TosEntity tosEntity=JSON.toJavaObject(JSONObject.parseObject(set.getTos()),TosEntity.class);
            json.put("domain",tosEntity.getDomain());
        }else{
            throw new Error("未配置上传信息");
        }
        json.put("visible",set.getVisible());
        return json;
    }
}
