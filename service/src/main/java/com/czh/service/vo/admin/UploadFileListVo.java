package com.czh.service.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件库列表出参
 * <p>
 * 不再直接返回 UploadFiles 实体，字段由本 VO 显式声明。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileListVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    /** 文件保存在哪里的 0虚拟文件夹 1七牛 2阿里oss 3腾讯cos */
    private Integer domain;
    /** 文件类型 1图片 2视频 3Excel 4word 5pdf 6zip 7未知 8文件夹 */
    private Integer type;
    /** 文件名称 */
    private String name;
    /** 上传到第三方的key */
    private String key;
    /** 文件地址 */
    private String url;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date atime;
    /** 上级目录ID 顶级目录为0 */
    private Integer pid;
    /** 图片或视频宽，其它为0 */
    private Integer fileWidth;
    /** 图片或视频高，其它为0 */
    private Integer fileHeight;
    /** 文件大小kb */
    private Integer fileSize;
    /** 图片缩略图或视频封面图 */
    private String thumb;
}
