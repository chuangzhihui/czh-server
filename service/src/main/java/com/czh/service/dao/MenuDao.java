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

    /**
     * BE-30：一次性取出该角色有权限的全部显示菜单（扁平），供内存构树使用。
     * 替掉原来"每个节点查一次"的递归 N+1。
     *
     * @param ids 角色有权限的菜单 id 列表（逗号分隔）
     * @return 扁平菜单列表，已按 sort 升序
     */
    List<Menu> selectAuthMenuFlat(String ids);

    List<GetMenusByPidVo> getMenusByPid(Integer pid);

    List<GetMenuListVo> getMenuListByPid(Integer pid);
}
