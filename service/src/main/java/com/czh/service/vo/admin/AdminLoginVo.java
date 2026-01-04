package com.czh.service.vo.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminLoginVo {
    @NotBlank
    @NotNull
    private String avatar;
    @NotBlank
    @NotNull
    private String nickname;
    @NotBlank
    @NotNull
    private String token;

}
