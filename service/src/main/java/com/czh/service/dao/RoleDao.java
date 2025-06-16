package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.common.vo.SelectVo;
import com.czh.service.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 管理员角色表(Role)表数据库访问层
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface RoleDao extends BaseMapper<Role> {

    List<SelectVo> getRoleSelectList();
}
