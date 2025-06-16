package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.common.dto.PageDto;
import com.czh.common.vo.SelectVo;
import com.czh.service.entity.Role;
import com.czh.service.vo.admin.AddRoleGetMenusVo;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 管理员角色表(Role)表服务接口
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface RoleService extends IService<Role> {

    /**
     * 获取角色列表
     * @param req
     * @return
     */
    PageInfo<Role> getRoleList(PageDto req);

    /**
     * 添加角色时获取菜单可选性
     * @return
     */
    List<AddRoleGetMenusVo> getAddRoleMenus();

    /**
     * 获取角色下拉列表数据
     * @return
     */
    List<SelectVo> getRoleSelectList();
    /**
     * 根据角色ID获取权限列表
     */
    List<String> getRolePermissions(Integer roleId);

    /**
     * 更新角色缓存
     * @param roleId
     */
    void updateRoleCache(Integer roleId);
}
