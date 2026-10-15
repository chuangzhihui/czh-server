package com.czh.service.dto.admin;

import com.czh.common.utils.txCosUtil.entity.TxCosEntity;
import lombok.Data;

@Data
public class TxCosDto extends TxCosEntity {
    private int visible;
}
