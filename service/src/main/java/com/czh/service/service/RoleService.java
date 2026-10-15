package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.common.dto.PageDto;
import com.czh.common.vo.SelectVo;
import com.czh.service.entity.Role;
import com.czh.service.vo.admin.AddRoleGetMenusVo;
import com.czh.service.vo.admin.RoleListVo;
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
    PageInfo<RoleListVo> getRoleList(PageDto req);

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

    /**
     * 失效所有角色的权限缓存。
     * <p>
     * 用于<b>菜单 route 变更</b>这类无法定位到具体角色的场景：权限串来自 menu.route，
     * 而「哪些角色引用了这个菜单」无法从缓存侧得知，只能全量清。
     * <p>
     * 通过索引集合枚举已缓存的角色，不做 {@code keys} 全库扫描。
     */
    void evictAllRolePermissionCache();
}
