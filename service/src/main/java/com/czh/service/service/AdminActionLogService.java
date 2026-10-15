package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.service.dto.admin.AdminLogDto;
import com.czh.service.entity.AdminActionLog;
import com.czh.service.vo.admin.AdminActionLogVo;
import com.github.pagehelper.PageInfo;

/**
 * 后台管理员操作日志表(AdminActionLog)表服务接口
 * 数据源：MySQL 表 czh_admin_action_log
 */
public interface AdminActionLogService extends IService<AdminActionLog> {

    /**
     * 保存操作日志（由 AsyncService 异步调用）
     */
    void saveAdminActionLog(AdminActionLog adminActionLog);

    /**
     * 操作日志分页查询
     */
    PageInfo<AdminActionLogVo> adminLog(AdminLogDto req);
}
