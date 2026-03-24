package com.czh.common.utils.tosUtil;

import com.czh.common.exception.ErrorException;
import com.czh.common.utils.FileUtil;
import com.czh.common.utils.MediaUtils;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadVo;
import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.TosException;
import com.volcengine.tos.comm.HttpMethod;
import com.volcengine.tos.model.object.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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

    public LocalUploadVo uploadFile(MultipartFile file, String fileKey,Integer type) {
        File tempFile=null;
        try {
            byte[] bytes = file.getBytes();
            TOSV2 tos=new TOSV2ClientBuilder().build(tosEntity.getRegion(), tosEntity.getEndpoint(), tosEntity.getAccessKey(), tosEntity.getSecretKey());
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            PutObjectInput putObjectInput = new PutObjectInput()
                    .setBucket(tosEntity.getBucket()).setKey(fileKey).setContent(stream);
            tos.putObject(putObjectInput);
            LocalUploadVo vo=new LocalUploadVo();
            vo.setName(file.getOriginalFilename());
            vo.setUrl(tosEntity.getDomain()+"/"+fileKey);
            vo.setKey(fileKey);
            long bytesLong = file.getSize(); // 获取字节数
            int sizeInKB = Math.round((float) bytesLong / 1024);
            vo.setFileSize(Math.max(sizeInKB, 0));
            vo.setFileWidth(0);
            vo.setFileHeight(0);
            if(type.equals(1))
            {
                //图片
                vo.setThumb(vo.getUrl()+"?imageMogr2/thumbnail/160x160");
                BufferedImage bufferedImage= MediaUtils.multipartFileToBufferedImage(file);
                if(bufferedImage!=null)
                {
                    vo.setFileHeight(bufferedImage.getHeight());
                    vo.setFileWidth(bufferedImage.getWidth());
                }
            }else if(type.equals(2))
            {
                //视频
                vo.setThumb(vo.getUrl()+"?x-tos-process=video/snapshot,t_100");
                tempFile= FileUtil.multipartFileToTempFile(file);//视频文件
                //获取视频文件信息
                MediaUtils.VideoMeta videoMeta=MediaUtils.getVideoMeta(tempFile.getPath());
                vo.setFileWidth(videoMeta.getWidth());
                vo.setFileHeight(videoMeta.getHeight());
                vo.setFileSize((int)videoMeta.getDuration());
            }
            return vo;
        } catch (IOException e) {
            throw new ErrorException("文件解析失败");
        } catch (TosException e)
        {
            throw new ErrorException("上传到火山云失败!");
        }finally {
            if(tempFile!=null)
            {
                tempFile.delete();
            }
        }

    }
}
