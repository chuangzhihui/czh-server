package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.service.dto.admin.GetFileListDto;
import com.czh.service.entity.UploadFiles;
import com.czh.service.vo.admin.UploadLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 文件库(UploadFiles)表数据库访问层
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface UploadFilesDao extends BaseMapper<UploadFiles> {

    List<UploadFiles> selectByPid(GetFileListDto req);

    List<UploadLog> getImgList();
}
