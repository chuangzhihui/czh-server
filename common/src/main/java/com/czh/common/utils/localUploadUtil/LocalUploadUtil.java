package com.czh.common.utils.localUploadUtil;

import com.czh.common.exception.ErrorException;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
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
    public LocalUploadVo upload(MultipartFile file,String dir)
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
        String[] imgTypes=new String[]{"jpg","jpeg","png","gif"};
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


        //生成缩略图
        if(Arrays.asList(imgTypes).contains(extName))
        {
            //图片
            String[] keyArr=localUploadVo.getKey().split("\\.");
            String destPath=keyArr[0]+"_thumb."+extName;
            String[] namesArr=destPath.split("/");
            String thumbName=namesArr[namesArr.length -1];
            String srcFilePath=localUploadVo.getKey();
            try {
                compressByQuality(srcFilePath,destPath,0.5f);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorException(e.getMessage());
            }
            String thumb= config.getDomain()+extPath+thumbName;
            localUploadVo.setThumb(thumb);
        }
        return localUploadVo;
    }
    /**
     * 压缩图片
     * @param srcFilePath 源图片路径
     * @param destFilePath 目标图片路径
     * @param quality 质量参数(0.0-1.0)
     * @return
     * @throws IOException
     */
    private void compressByQuality(String srcFilePath, String destFilePath, float quality) throws IOException {
        // 验证源文件是否存在
        File srcFile = new File(srcFilePath);
        if (!srcFile.exists()) {
            throw new IOException("源文件不存在: " + srcFilePath);
        }
        if (!srcFile.isFile()) {
            throw new IOException("路径不是文件: " + srcFilePath);
        }

        // 读取图片并检查是否成功
        BufferedImage srcImage = ImageIO.read(srcFile);
        if (srcImage == null) {
            throw new IOException("无法读取图片文件，可能是不支持的格式: " + srcFilePath);
        }

        // 获取图片格式（处理不带扩展名的情况）
        String format = getFileFormat(srcFilePath);
        if (format == null) {
            throw new IOException("无法确定图片格式: " + srcFilePath);
        }

        try (OutputStream out = new FileOutputStream(destFilePath)) {
            // 获取指定格式的ImageWriter
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (!writers.hasNext()) {
                throw new IOException("找不到支持的图片格式: " + format);
            }

            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            // 对于支持压缩的格式设置压缩质量
            if (param.canWriteCompressed()) {
                // 确保质量参数在有效范围内
                quality = Math.max(0.0f, Math.min(1.0f, quality));
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            // 写入图片（确保图片对象不为空）
            if (srcImage != null) {
                writer.setOutput(ImageIO.createImageOutputStream(out));
                writer.write(null, new IIOImage(srcImage, null, null), param);
            } else {
                throw new IOException("图片对象为空，无法写入");
            }

            writer.dispose();
        }
    }
    private  String getFileFormat(String filePath) {
        int lastDotIndex = filePath.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
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
