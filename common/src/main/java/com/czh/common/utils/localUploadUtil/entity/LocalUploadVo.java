package com.czh.common.utils.localUploadUtil.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocalUploadVo {
    @NotNull
    //文件名称
    private String name;
    @NotNull
    private Integer domain;//上传方式
    @NotNull
    //文件类型  1图片 2视频 3 Excel 4 word 5 pdf 6 zip 7 未知类型文件 8文件夹
    private Integer type;
    @NotNull
    private String url;//文件url
    @NotNull
    private String key;//文件key
    @NotNull
    private Integer fileWidth;//图片或者视频宽其它为0
    @NotNull
    private Integer fileHeight;//图片或者视频高其它为0
    @NotNull
    private Integer fileSize;//文件大小kb
    @NotNull
    private String thumb;//图片的缩略图或者视频的封面图 其它为空字符串
}
