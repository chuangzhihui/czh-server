package com.czh.common.utils.aliOssUtil;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.PolicyConditions;
import com.czh.common.exception.ErrorException;
import com.czh.common.utils.FileUtil;
import com.czh.common.utils.MediaUtils;
import com.czh.common.utils.aliOssUtil.entity.AliOssEntity;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
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
    public LocalUploadVo uploadFile(MultipartFile file, String key,Integer type){
        File tempFile=null;
        try{
            OSS client=new OSSClientBuilder().build(endpoint,AccessKeyID,AccessKey);
            byte[] uploadBytes= file.getBytes();
            client.putObject(bucket, key, new ByteArrayInputStream(uploadBytes));
            // 关闭OSSClient。
            client.shutdown();
            LocalUploadVo vo=new LocalUploadVo();
            vo.setName(file.getOriginalFilename());
            vo.setUrl(domain+"/"+key);
            vo.setKey(key);
            long bytes = file.getSize(); // 获取字节数
            int sizeInKB = Math.round((float) bytes / 1024);
            vo.setFileSize(Math.max(sizeInKB, 0));
            vo.setFileWidth(0);
            vo.setFileHeight(0);
            if(type.equals(1))
            {
                //图片
                vo.setThumb(vo.getUrl()+"?x-oss-process=image/resize,h_160");
                BufferedImage bufferedImage= MediaUtils.multipartFileToBufferedImage(file);
                if(bufferedImage!=null)
                {
                    vo.setFileHeight(bufferedImage.getHeight());
                    vo.setFileWidth(bufferedImage.getWidth());
                }
            }else if(type.equals(2))
            {
                //视频
                vo.setThumb(vo.getUrl()+"?x-oss-process=video/snapshot,t_1,f_jpg");
                tempFile= FileUtil.multipartFileToTempFile(file);//视频文件
                //获取视频文件信息
                MediaUtils.VideoMeta videoMeta=MediaUtils.getVideoMeta(tempFile.getPath());
                vo.setFileWidth(videoMeta.getWidth());
                vo.setFileHeight(videoMeta.getHeight());
                vo.setFileSize((int)videoMeta.getDuration());
            }
            return vo;
        }catch (Exception e){
            e.printStackTrace();
            throw new ErrorException("文件解析失败!");
        }finally {
            if(tempFile!=null)
            {
                tempFile.delete();
            }
        }
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
