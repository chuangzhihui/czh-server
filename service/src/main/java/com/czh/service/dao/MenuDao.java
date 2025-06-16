package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.service.entity.Menu;
import com.czh.service.vo.admin.AuthMenuVo;
import com.czh.service.vo.admin.GetMenuListVo;
import com.czh.service.vo.admin.GetMenusByPidVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * (Menu)表数据库访问层
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface MenuDao extends BaseMapper<Menu> {

    List<AuthMenuVo> getAuthMenuList(Integer pid, String ids);

    List<GetMenusByPidVo> getMenusByPid(Integer pid);

    List<GetMenuListVo> getMenuListByPid(Integer pid);
}
