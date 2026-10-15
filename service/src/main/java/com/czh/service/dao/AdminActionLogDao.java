package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.service.dto.admin.AdminLogDto;
import com.czh.service.entity.AdminActionLog;
import com.czh.service.vo.admin.AdminActionLogVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 后台管理员操作日志表(AdminActionLog)表数据库访问层
 */
public interface AdminActionLogDao extends BaseMapper<AdminActionLog> {

    /**
     * 操作日志分页列表（条件：管理员ID、IP、操作地址、操作描述、时间区间）
     * 排序固定按 createTime 倒序
     */
    List<AdminActionLogVo> getAdminLogList(AdminLogDto req);
}
