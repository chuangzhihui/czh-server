package com.czh.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.service.dao.AdminActionLogDao;
import com.czh.service.dto.admin.AdminLogDto;
import com.czh.service.entity.AdminActionLog;
import com.czh.service.service.AdminActionLogService;
import com.czh.service.vo.admin.AdminActionLogVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台管理员操作日志表(AdminActionLog)表服务实现类
 * 数据源：MySQL
 */
@Service
public class AdminActionLogServiceImpl extends ServiceImpl<AdminActionLogDao, AdminActionLog> implements AdminActionLogService {

    /**
     * 保存操作日志（由 AsyncService 异步调用）
     */
    @Override
    public void saveAdminActionLog(AdminActionLog adminActionLog) {
        baseMapper.insert(adminActionLog);
    }

    /**
     * 操作日志分页查询
     */
    @Override
    public PageInfo<AdminActionLogVo> adminLog(AdminLogDto req) {
        // 分页与排序：排序固定在 XML 中按 createTime desc，避免 orderBy 直接拼接带来的注入风险
        PageHelper.startPage(req.getPage(), req.getSize());
        List<AdminActionLogVo> list = baseMapper.getAdminLogList(req);
        return new PageInfo<>(list);
    }
}
