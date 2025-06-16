package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.service.entity.UploadSet;
import org.apache.ibatis.annotations.Mapper;
@Mapper
/**
 * 文件上传配置(UploadSet)表数据库访问层
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface UploadSetDao extends BaseMapper<UploadSet> {

}
