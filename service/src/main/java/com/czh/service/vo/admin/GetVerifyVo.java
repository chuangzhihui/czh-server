package com.czh.service.vo.admin;

import lombok.Data;

@Data
public class GetVerifyVo {
    private String img;//图片base64
    private String uuid;//图片验证标识
}
