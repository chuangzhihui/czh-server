package com.czh.service.service;

import com.czh.service.dto.admin.AdminLogDto;
import com.czh.service.entity.AdminActionLog;
import com.github.pagehelper.PageInfo;

public interface AdminActionLogService {
    void saveAdminActionLog(AdminActionLog adminActionLog);

    PageInfo<AdminActionLog> adminLog(AdminLogDto req);
}
