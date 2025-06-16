package com.czh.common.utils.aliOssUtil;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.PolicyConditions;
import com.czh.common.utils.aliOssUtil.entity.AliOssEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class AliOssUtil {
    private String AccessKeyID;
    private String AccessKey;
    private String endpoint;
    private String bucket;
    private String domain;
    public AliOssUtil(AliOssEntity aliOssEntity){
        this.AccessKeyID=aliOssEntity.getAk();
        this.AccessKey=aliOssEntity.getSk();
        this.endpoint=aliOssEntity.getEndpoint();
        this.bucket=aliOssEntity.getBucket();
        this.domain=aliOssEntity.getDomain();
    }
    //删除文件
    public void  delFile(String key){
        OSS client=new OSSClientBuilder().build(endpoint,AccessKeyID,AccessKey);
        // 删除文件或目录。如果要删除目录，目录必须为空。
        client.deleteObject(bucket, key);
        // 关闭OSSClient。
        client.shutdown();
    }
    public void uploadFile(byte[] uploadBytes,String key){
        OSS client=new OSSClientBuilder().build(endpoint,AccessKeyID,AccessKey);
        client.putObject(bucket, key, new ByteArrayInputStream(uploadBytes));
        // 关闭OSSClient。
        client.shutdown();
    }
    public Map<String,Object>  getToken(){
        OSS client=new OSSClientBuilder().build(endpoint,AccessKeyID,AccessKey);

        try{
            long expireTime = 300;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);
            Map<String,Object> json=new HashMap<>();
            json.put("policy",encodedPolicy);
            json.put("signature",postSignature);
            json.put("OSSAccessKeyId",AccessKeyID);
            json.put("domain",domain);
            return json;
        }catch (Exception e){
            throw new RuntimeException("上传失败!");
        }finally {
            client.shutdown();
        }
    }
}
