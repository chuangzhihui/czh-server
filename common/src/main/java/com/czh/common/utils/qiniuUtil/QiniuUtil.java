package com.czh.common.utils.qiniuUtil;

import com.czh.common.utils.qiniuUtil.entity.QiniuEntity;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class QiniuUtil {
    private QiniuEntity qiniuEntity;
    public QiniuUtil(QiniuEntity qiniuEntity){
       this.qiniuEntity = qiniuEntity;
    }
    private Auth getAuth(){
        return Auth.create(qiniuEntity.getAccessKey(),qiniuEntity.getSecretKey());
    }
    /**
     * 获取七牛上传的token
     * @param key 文件key
     * @return
     */
    public String getToken(String key){
        Auth auth=getAuth();
        return auth.uploadToken(qiniuEntity.getBucket(), key);
    }
    public String getToken(){
        Auth auth=getAuth();
        return auth.uploadToken(qiniuEntity.getBucket());
    }
    //上传文件
    public void  uploadFile(byte[] uploadBytes,String key){
        Configuration cfg=new Configuration(Region.region2());
        UploadManager uploadManager = new UploadManager(cfg);
        String upToken=getToken(key);
        log.info("上传token:{}",upToken);
        try {
            Response response = uploadManager.put(uploadBytes, key, upToken);
            //解析上传成功的结果
            log.info("上传结果:{}",response.bodyString());
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.error("上传失败:{}",r.toString());
        }
    }
    //删除文件
    public void delFile(String key){
        Configuration cfg=null;
        switch (qiniuEntity.getEndpoint())
        {
            case "up.qiniup.com":
                cfg=new Configuration(Region.region0());
                break;
            case "up-z1.qiniup.com":
                cfg=new Configuration(Region.region1());
                break;
            case "up-z2.qiniup.com":
                cfg=new Configuration(Region.region2());
                break;
            default:
                cfg=new Configuration(Region.region0());
                break;
        }
        Auth auth=getAuth();
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            log.info("删除key:{}",key);
            bucketManager.delete(qiniuEntity.getBucket(), key);
            log.error("删除七牛云文件成功:{}",key);
        } catch (QiniuException ex) {
            //如果遇到异常，说明删除失败
            log.error("删除七牛云文件失败:{}",ex.response.toString());
        }
    }
}
