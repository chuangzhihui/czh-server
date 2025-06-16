package com.czh.service.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 * 系统设置表(Setting)表实体类
 *
 * @author makejava
 * @since 2025-06-05 22:33:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Setting implements Serializable {
private static final long serialVersionUID=1L;
    
    @TableId(type=IdType.AUTO)
    private Integer id;
    //配置标题    
    private String title;
    //1 文本  2图片      
    private Integer type;
    
    private String value;
    //是否允许删除
    private Integer canDel;
}
