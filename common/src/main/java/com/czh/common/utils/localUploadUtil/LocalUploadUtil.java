package com.czh.common.utils.localUploadUtil;

import com.czh.common.exception.ErrorException;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

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
     */
    public LocalUploadVo upload(MultipartFile file)
    {
        String uploadPath=config.getPath();//上传路径

        String extPath="/"+DateUtil.getStrDate("yyyyMMdd")+"/";
        uploadPath+=extPath;
        if(file.isEmpty()){
           throw new ErrorException("文件信息为空!");
        }
        String originalFilename=file.getOriginalFilename();
        String[] arr=originalFilename.split("\\.");
        String extName=arr[arr.length -1].toLowerCase(Locale.ROOT);
        String[] allowFiles=new String[]{"zip","doc","docx","rar","png","jpeg","jpg","xls","xlsx","pdf","mp4","avi","flv"};
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
        localUploadVo.setKey(uploadPath+filename);
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
