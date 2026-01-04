package com.czh.service.vo.admin;

import com.czh.common.utils.aliOssUtil.entity.AliOssEntity;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import com.czh.common.utils.qiniuUtil.entity.QiniuEntity;
import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.czh.common.utils.txCosUtil.entity.TxCosEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetUploadConfigVo {
    @NotNull
    private Integer visible;//0未选用 1七牛 2阿里 3腾讯 4本地 5 火山云tos
    @NotNull
    private QiniuEntity qiniu;//七牛云配置
    @NotNull
    private AliOssEntity alioss;//阿里OSS配置
    @NotNull
    private TxCosEntity txcos;//腾讯COS配置
    @NotNull
    private TosEntity tos;//火山云配置
    @NotNull
    private LocalUploadConfig local;//本地配置
}
