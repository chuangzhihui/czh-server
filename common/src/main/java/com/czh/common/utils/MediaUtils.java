package com.czh.common.utils;

import com.czh.common.exception.ErrorException;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Iterator;

public class MediaUtils {
    /**
     * 图片文件转BufferedImage
     * @param imageFile
     * @return
     */
    public static BufferedImage ImageFileToBufferedImage(File imageFile)
    {
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            System.out.println("图片文件不存在或不是有效文件");
            return null;
        }
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(imageFile)) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static BufferedImage multipartFileToBufferedImage(MultipartFile file)
    {
        try (InputStream inputStream = file.getInputStream()) {
           return ImageIO.read(inputStream);
        } catch (IOException e) {
            System.out.println("读取图片流失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 压缩图片（按尺寸比例+质量压缩）
     * @param srcFile 源图片文件
     * @param destFile 压缩后保存的文件
     * @param scale 缩放比例（0.1-1.0，1.0不缩放）
     * @param quality 压缩质量（0.0-1.0，值越小压缩率越高，画质越差）
     * @return 压缩是否成功
     */
    public static boolean compressImage(File srcFile, File destFile, double scale, float quality) {
        if (srcFile == null || !srcFile.exists() || scale <= 0 || scale > 1 || quality < 0 || quality > 1) {
            System.out.println("压缩参数无效");
            return false;
        }

        try {
            BufferedImage srcImage = ImageIO.read(srcFile);
            if (srcImage == null) {
                System.out.println("源图片读取失败");
                return false;
            }

            int srcWidth = srcImage.getWidth();
            int srcHeight = srcImage.getHeight();
            int newWidth = (int) (srcWidth * scale);
            int newHeight = (int) (srcHeight * scale);

            BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = newImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(srcImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            String format = getImageFormat(srcFile);
            if (format == null) {
                format = "jpg";
            }

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (!writers.hasNext()) {
                System.out.println("不支持的图片格式：" + format);
                return false;
            }

            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            try (FileOutputStream fos = new FileOutputStream(destFile);
                 ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(newImage, null, null), param);
                writer.dispose();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取图片格式（小写，如jpg/png）
     */
    private static String getImageFormat(File file) {
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) {
            return null;
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }



    public static class VideoMeta{
        private Integer width;       // 宽度
        private Integer height;      // 高度
        private double duration;     // 时长（秒）

        // Getter & Setter
        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }
        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }
        public double getDuration() { return duration; }
        public void setDuration(double duration) { this.duration = duration; }
    }

    public static VideoMeta getVideoMeta(String videoPath)
    {
        File videoFile = new File(videoPath);
        // 校验文件是否存在
        if (!videoFile.exists()) {
            throw new ErrorException("视频文件不存在" );
        }
        // 构建FFprobe实例（优先使用指定路径，否则调用系统FFprobe）
        FFprobe ffprobe = FFprobe.atPath();
        // 执行FFprobe获取视频信息
        FFprobeResult probeResult = ffprobe
                .setShowStreams(true)  // 显示流信息
                .setShowFormat(true)   // 显示格式信息
                .setInput(videoPath)
                .execute();
        VideoMeta videoMeta = new VideoMeta();
        // 提取视频流信息（宽高、编码）
        probeResult.getStreams().stream()
                .filter(stream -> stream.getCodecType() == StreamType.VIDEO)
                .findFirst()
                .ifPresent(videoStream -> {
                    videoMeta.setWidth(videoStream.getWidth());
                    videoMeta.setHeight(videoStream.getHeight());
                });
        // 提取格式信息（时长、格式）
        if (probeResult.getFormat() != null) {
            videoMeta.setDuration(Double.valueOf(probeResult.getFormat().getDuration()));
        }
        return videoMeta;
    }

    /**
     * 提取视频封面图
     * @param videoPath 视频文件路径
     * @param coverPath 封面图输出路径（建议jpg格式）
     * @param timePoint 截取时间点（秒，默认0秒即第一帧）
     * @throws Exception 异常
     */
    public static boolean extractCover(String videoPath, String coverPath, long timePoint){
        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
//            throw new IllegalArgumentException("视频文件不存在：" + videoPath);
            return false;
        }

        // 构建FFmpeg实例
        FFmpeg ffmpeg = FFmpeg.atPath();

        // 2. 构建FFmpeg命令（0.10.0需手动添加原生参数）
        ffmpeg.addInput(UrlInput.fromPath(Paths.get(videoPath))
                        .addArguments("-ss", String.valueOf(timePoint))) // 截取时间点
                .addOutput(UrlOutput.toPath(Paths.get(coverPath))
                        .addArguments("-vframes", "1")  // 仅取1帧（替代setFrames）
//                        .addArgument("-y")// 覆盖已存在的文件
                )
                .execute();

        // 校验封面
        File coverFile = new File(coverPath);
        if (!coverFile.exists() || coverFile.length() == 0) {
            return false;
//            throw new RuntimeException("封面提取失败：" + coverPath);
        }
        System.out.println("封面提取成功：" + coverPath);
        return true;
    }
}
