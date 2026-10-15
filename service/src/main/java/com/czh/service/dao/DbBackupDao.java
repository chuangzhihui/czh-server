package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.service.dto.admin.DbBackUpListDto;
import com.czh.service.entity.DbBackup;
import com.czh.service.vo.admin.DbBackupVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 数据库备份记录(DbBackup)表数据库访问层
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface DbBackupDao extends BaseMapper<DbBackup> {

    List<DbBackupVo> getList(DbBackUpListDto dto);
}
