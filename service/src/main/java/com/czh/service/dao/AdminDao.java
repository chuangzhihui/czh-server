package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.common.vo.SelectVo;
import com.czh.service.dto.admin.AdminListDto;
import com.czh.service.entity.Admin;
import com.czh.service.vo.admin.AdminListVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 管理员表(Admin)表数据库访问层
 *
 * @author makejava
 * @since 2025-06-05 22:18:32
 */
public interface AdminDao extends BaseMapper<Admin> {


    List<AdminListVo> getAdminList(AdminListDto req);

    List<SelectVo> getAdminSelectList();
}
