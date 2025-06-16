package com.czh.common.utils.tosUtil;

import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.comm.HttpMethod;
import com.volcengine.tos.model.object.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class TosUtil {
    private TosEntity tosEntity;
    public TosUtil(TosEntity entity) {
        this.tosEntity = entity;
    }
    public Map<String,Object> getUploadUrl(String key) {
        TOSV2 client=new TOSV2ClientBuilder().build(tosEntity.getRegion(), tosEntity.getEndpoint(), tosEntity.getAccessKey(), tosEntity.getSecretKey());
        PreSignedURLInput input = new PreSignedURLInput().setBucket(tosEntity.getBucket()).setKey(key)
                .setHttpMethod(HttpMethod.PUT).setExpires(3600);
        PreSignedURLOutput output = client.preSignedURL(input);
        Map<String,Object> map=new HashMap<>();
        map.put("url", output.getSignedUrl());
        map.put("header", output.getSignedHeader());
        map.put("domain", tosEntity.getDomain());
        return map;
    }

    public void delFile(String key) {
        TOSV2 tos=new TOSV2ClientBuilder().build(tosEntity.getRegion(), tosEntity.getEndpoint(), tosEntity.getAccessKey(), tosEntity.getSecretKey());
        DeleteObjectInput input = new DeleteObjectInput().setBucket(tosEntity.getBucket()).setKey(key);
        DeleteObjectOutput output = tos.deleteObject(input);
    }

    public void uploadFile(byte[] bytes, String fileKey) {
        TOSV2 tos=new TOSV2ClientBuilder().build(tosEntity.getRegion(), tosEntity.getEndpoint(), tosEntity.getAccessKey(), tosEntity.getSecretKey());
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        PutObjectInput putObjectInput = new PutObjectInput()
                .setBucket(tosEntity.getBucket()).setKey(fileKey).setContent(stream);
        tos.putObject(putObjectInput);
    }
}
