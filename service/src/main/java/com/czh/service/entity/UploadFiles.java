package com.czh.service.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件库(UploadFiles)表实体类
 *
 * @author makejava
 * @since 2025-06-05 22:33:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFiles implements Serializable {
private static final long serialVersionUID=1L;
    
    @TableId(type=IdType.AUTO)
    private Integer id;
    //文件保存在哪里的 0虚拟文件夹 1七牛 2阿里oss 3腾讯cos    
    private Integer domain;
    //文件类型  1图片 2视频 3 Excel 4 word 5 pdf 6 zip 7 未知类型文件 8文件夹    
    private Integer type;
    //文件名称    
    private String name;
    //上传到第三方的key
    @TableField("`key`")
    private String key;
    //文件地址    
    private String url;
    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date atime;
    //上级目录ID 顶级目录为0    
    private Integer pid;
}
