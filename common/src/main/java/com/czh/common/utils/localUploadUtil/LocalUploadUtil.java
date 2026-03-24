package com.czh.common.utils.localUploadUtil;

import com.czh.common.exception.ErrorException;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.MediaUtils;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

@Slf4j
public class LocalUploadUtil {
    private LocalUploadConfig config;
    public LocalUploadUtil(LocalUploadConfig config) {
        this.config = config;

    }

    /**
     * 执行文件上传
     * @param file
     * @param dir
     * @param type 文件类型  1图片 2视频 3 Excel 4 word 5 pdf 6 zip 7 未知类型文件 8文件夹
     * @return
     */
    public LocalUploadVo upload(MultipartFile file,String dir,Integer type)
    {
        String uploadPath=config.getPath();//上传路径-都不要加/
        String extPath="";
        if(!dir.isEmpty())
        {
            extPath="/"+dir;
        }
        extPath+="/"+ DateUtil.getStrDate("yyyyMMdd")+"/";
        uploadPath+=extPath;
        if(file.isEmpty()){
           throw new ErrorException("文件信息为空!");
        }
        String originalFilename=file.getOriginalFilename();
        String[] arr=originalFilename.split("\\.");
        String extName=arr[arr.length -1].toLowerCase(Locale.ROOT);
        String[] allowFiles=new String[]{"zip","doc","docx","rar","png","jpeg","jpg","xls","xlsx","pdf","mp4","avi","mkv"};
        if(!Arrays.asList(allowFiles).contains(extName))
        {
           throw new ErrorException("不支持上传此类文件:"+extName);
        }
        String filename=checkFile(file.getOriginalFilename(),uploadPath);
        if(extName.equals("pdf"))
        {
            try {
                PDDocument document = PDDocument.load(file.getInputStream());
                PDFTextStripper pdfStripper = new PDFTextStripper();
                pdfStripper.getText(document);
            } catch (IOException e) {
                throw new ErrorException("PDF文档疑似存在XSS攻击脚本!禁止上传!");
            }

        }
        try {
            byte[] bytes=file.getBytes();
            File filepath=new File(uploadPath);

            if(!filepath.exists()){
                filepath.mkdirs();
            }
            Path path= Paths.get(uploadPath+filename);
            Files.write(path,bytes);
        }catch (IOException e){
            e.printStackTrace();
            throw new ErrorException("上传失败!");
        }
        LocalUploadVo localUploadVo=new LocalUploadVo();
        localUploadVo.setName(filename);
        localUploadVo.setUrl(config.getDomain()+extPath+filename);
        log.info("uploadPath:{}",uploadPath);
        log.info("filename:{}",filename);
        localUploadVo.setKey(uploadPath+filename);

        //上传后的文件对象
        File uploadedFile= new File(localUploadVo.getKey());
        //计算文件大小
        long bytes = uploadedFile.length(); // 获取字节数
        int sizeInKB = Math.round((float) bytes / 1024);
        localUploadVo.setFileSize(Math.max(sizeInKB, 0));
        localUploadVo.setFileWidth(0);
        localUploadVo.setFileHeight(0);
        //生成缩略图
        if(type.equals(1))
        {
            //图片
            String[] keyArr=localUploadVo.getKey().split("\\.");
            String destPath=keyArr[0]+"_thumb."+extName;
            String[] namesArr=destPath.split("/");
            String thumbName=namesArr[namesArr.length -1];
            String srcFilePath=localUploadVo.getKey();
            boolean isThumb=MediaUtils.compressImage(new File(srcFilePath),new File(destPath),0.5f,0.5f);
            String thumb= isThumb?(config.getDomain()+extPath+thumbName): localUploadVo.getUrl();
            localUploadVo.setThumb(thumb);
            //获取图片文件尺寸
            BufferedImage bufferedImage= MediaUtils.ImageFileToBufferedImage(uploadedFile);
            if(bufferedImage!=null)
            {
                localUploadVo.setFileWidth(bufferedImage.getWidth());
                localUploadVo.setFileHeight(bufferedImage.getHeight());
            }
        }else if(type.equals(2))
        {
            //视频文件
            //生成缩略图
            String[] keyArr=localUploadVo.getKey().split("\\.");
            String destPath=keyArr[0]+"_thumb.png";
            String[] namesArr=destPath.split("/");
            String thumbName=namesArr[namesArr.length -1];
            String srcFilePath=localUploadVo.getKey();
            boolean isThumb=MediaUtils.extractCover(srcFilePath,destPath,1);
            String thumb= isThumb?(config.getDomain()+extPath+thumbName): localUploadVo.getUrl();
            localUploadVo.setThumb(thumb);
            MediaUtils.VideoMeta videoMeta=MediaUtils.getVideoMeta(srcFilePath);
            localUploadVo.setFileWidth(videoMeta.getWidth());
            localUploadVo.setFileHeight(videoMeta.getHeight());
            localUploadVo.setFileSize((int)videoMeta.getDuration());
        }
        return localUploadVo;
    }

    private String checkFile(String filename,String uploadPath){
        String[] arr=filename.split("\\.");
        String name=filename.replace("."+arr[arr.length -1],"");
        String extName=arr[arr.length -1];
        if(extName.equals("doc"))
        {
            extName="docx";
            filename+="x";
        }
        File oldFile=new File(uploadPath+filename);
        if(oldFile.exists()){
            name=name+"1";
            return checkFile(name+"."+extName,uploadPath);
        }
        return filename;
    }





}
