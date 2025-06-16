package com.czh.service.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 * 文件上传配置(UploadSet)表实体类
 *
 * @author makejava
 * @since 2025-06-05 22:33:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadSet implements Serializable {
private static final long serialVersionUID=1L;
    
    @TableId(type=IdType.AUTO)
    private Integer id;
    
    private String qiniu;
    
    private String alioss;
    
    private String txcos;
    //火山云tos
    private String tos;
    //本地上传配置
    private String local;
    //选用的哪个 0未选用 1七牛 2阿里 3腾讯   4本地 5 火山云tos
    private Integer visible;
}
