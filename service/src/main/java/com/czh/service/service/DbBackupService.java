package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.service.dto.admin.DbBackUpListDto;
import com.czh.service.entity.DbBackup;
import com.czh.service.vo.admin.DbBackupVo;
import com.github.pagehelper.PageInfo;

/**
 * 数据库备份记录(DbBackup)表服务接口
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface DbBackupService extends IService<DbBackup> {


    void backup(Integer adminId);

    void restoreDbById(Integer id);

    PageInfo<DbBackupVo> getList(DbBackUpListDto dto);
}
