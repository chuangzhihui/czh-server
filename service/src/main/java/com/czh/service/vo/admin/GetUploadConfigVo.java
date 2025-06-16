package com.czh.service.vo.admin;

import com.czh.common.utils.aliOssUtil.entity.AliOssEntity;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import com.czh.common.utils.qiniuUtil.entity.QiniuEntity;
import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.czh.common.utils.txCosUtil.entity.TxCosEntity;
import lombok.Data;

@Data
public class GetUploadConfigVo {
    private int visible;
    private QiniuEntity qiniu;
    private AliOssEntity alioss;
    private TxCosEntity txcos;
    private TosEntity tos;
    private LocalUploadConfig local;
}
