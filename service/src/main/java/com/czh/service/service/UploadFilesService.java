package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.service.dto.admin.GetFileListDto;
import com.czh.service.entity.UploadFiles;
import com.czh.service.vo.admin.GetFileListVo;
import com.czh.service.vo.admin.UploadLog;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 文件库(UploadFiles)表服务接口
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface UploadFilesService extends IService<UploadFiles> {


    void removeFileById(Integer id);

    PageInfo<GetFileListVo> getFileList(GetFileListDto req);

    List<UploadLog> getImgList();
}
