package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.service.entity.UploadSet;
import com.czh.service.vo.admin.GetUploadTokenVo;

/**
 * 文件上传配置(UploadSet)表服务接口
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface UploadSetService extends IService<UploadSet> {


    GetUploadTokenVo getUploadToken();
}
