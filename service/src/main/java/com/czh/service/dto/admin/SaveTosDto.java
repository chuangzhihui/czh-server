package com.czh.service.dto.admin;

import com.czh.common.utils.tosUtil.entity.TosEntity;
import lombok.Data;

@Data
public class SaveTosDto extends TosEntity {
    private Integer visible;//是否启用
}
