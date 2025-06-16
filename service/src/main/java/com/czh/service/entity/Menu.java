package com.czh.service.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 * (Menu)表实体类
 *
 * @author makejava
 * @since 2025-06-05 22:33:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu implements Serializable {
    private static final long serialVersionUID=1L;
    
    @TableId(type=IdType.AUTO)
    private Integer id;
    //上级id    
    private Integer pid;
    //菜单名称    
    private String name;
    //前端路由    
    private String path;
    //后端路由    
    private String route;
    //菜单等级    
    private Integer level;
    //图标    
    private String icon;
    //排序 低到高    
    private Integer sort;
    //是否显示    
    private Integer display;
}
