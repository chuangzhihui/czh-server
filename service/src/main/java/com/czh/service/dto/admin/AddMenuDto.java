package com.czh.service.dto.admin;

import lombok.Data;

@Data
public class AddMenuDto {
    private int pid;//上级菜单ID
    private String name;//菜单名称
    private String path;//前端路由
    private String route;//后端路由
    private String icon;//图标
    private int display;//是否显示
    private int sort;//排序
    private int level;//菜单等级 最高三级
}
