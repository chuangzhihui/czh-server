package com.czh.service.dto.admin;

import com.czh.common.utils.qiniuUtil.entity.QiniuEntity;
import lombok.Data;

@Data
public class QiniuDto extends QiniuEntity {
    private int visible;
}
