package com.czh.service.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminLoginVo {

    private String avatar;
    private String nickname;
    private String token;

}
