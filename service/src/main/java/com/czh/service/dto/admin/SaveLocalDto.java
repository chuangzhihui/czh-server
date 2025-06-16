package com.czh.service.dto.admin;

import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import lombok.Data;

@Data
public class SaveLocalDto extends LocalUploadConfig {
    private Integer visible;//是否启用
}
