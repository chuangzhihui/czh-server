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
 * 管理员角色表(Role)表实体类
 *
 * @author makejava
 * @since 2025-06-05 22:33:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {
private static final long serialVersionUID=1L;
    
    @TableId(type=IdType.AUTO)
    private Integer roleId;
    
    private String roleName;
    
    private String ids;
    @TableField("`describe`")
    private String describe;
    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date atime;
}
