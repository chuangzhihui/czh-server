package com.czh.service.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据库备份记录(DbBackup)表实体类
 *
 * @author makejava
 * @since 2025-06-05 22:33:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbBackup implements Serializable {
private static final long serialVersionUID=1L;
    
    @TableId(type=IdType.AUTO)
    private Integer id;
    //备份文件名称    
    private String fileName;
    //备份人    
    private Integer adminId;
    //备份文件完整路径    
    private String filePath;
    //文件大小    
    private Integer fileSize;
    //备份时间    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date atime;
}
