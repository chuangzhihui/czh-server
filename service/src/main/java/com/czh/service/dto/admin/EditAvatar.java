package com.czh.service.dto.admin;

import lombok.Data;

@Data
public class EditAvatar {
    private String  avatar;//头像 头像和用户名不能同时为空
    private String  username;//用户名 头像和用户名不能同时为空
}
