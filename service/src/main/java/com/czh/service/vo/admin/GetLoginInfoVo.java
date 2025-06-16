package com.czh.service.vo.admin;

import lombok.Data;

import java.util.List;

@Data
public class GetLoginInfoVo {
    private String name;//系统名称
    private String username;//昵称
    private String avatar;//头像
    private List<AuthMenuVo> menus;//权限菜单
    private Integer changePwdType;//修改密码提示 0不提示 1警告提示 2强制修改
    private String changePwdTip;//修改密码提示
}
