package com.czh.common.utils.qiniuUtil;

import com.czh.common.exception.ErrorException;
import com.czh.common.utils.FileUtil;
import com.czh.common.utils.MediaUtils;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadVo;
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
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
    public LocalUploadVo uploadFile(MultipartFile file, String key,Integer type){
        Configuration cfg=new Configuration(Region.region2());
        UploadManager uploadManager = new UploadManager(cfg);
        String upToken=getToken(key);
        LocalUploadVo vo=new LocalUploadVo();
        File tempFile=null;
        try {

            byte[] uploadBytes= file.getBytes();
            Response response = uploadManager.put(uploadBytes, key, upToken);
            //解析上传成功的结果
            log.info("上传key:{}",key);
            log.info("上传结果:{}",response.bodyString());
            vo.setName(file.getOriginalFilename());
            vo.setUrl(qiniuEntity.getDomain()+"/"+key);
            vo.setKey(key);
            long bytes = file.getSize(); // 获取字节数
            int sizeInKB = Math.round((float) bytes / 1024);
            vo.setFileSize(Math.max(sizeInKB, 0));
            vo.setFileWidth(0);
            vo.setFileHeight(0);
            if(type.equals(1))
            {
                //图片
                vo.setThumb(vo.getUrl()+"?imageView2/1/w/160/h/160/q/50");
                BufferedImage bufferedImage= MediaUtils.multipartFileToBufferedImage(file);
                if(bufferedImage!=null)
                {
                    vo.setFileHeight(bufferedImage.getHeight());
                    vo.setFileWidth(bufferedImage.getWidth());
                }
            }else if(type.equals(2))
            {
                //视频
                vo.setThumb(vo.getUrl()+"?vframe/jpg/offset/1");
                tempFile= FileUtil.multipartFileToTempFile(file);//视频文件
                //获取视频文件信息
                MediaUtils.VideoMeta videoMeta=MediaUtils.getVideoMeta(tempFile.getPath());
                vo.setFileWidth(videoMeta.getWidth());
                vo.setFileHeight(videoMeta.getHeight());
                vo.setFileSize((int)videoMeta.getDuration());
            }
            return vo;
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.error("上传失败:{}",r.toString());
            throw new ErrorException("上传到七牛云失败!");
        } catch (IOException e) {
            throw new ErrorException("文件解析错误!");
        }finally {
            if(tempFile!=null){
                tempFile.delete();
            }
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
