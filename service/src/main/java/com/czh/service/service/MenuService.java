package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.common.dto.PageDto;
import com.czh.service.dto.admin.EditMenuDto;
import com.czh.service.entity.Menu;
import com.czh.service.vo.admin.AuthMenuVo;
import com.czh.service.vo.admin.GetMenuListVo;
import com.czh.service.vo.admin.GetMenusByPidVo;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * (Menu)表服务接口
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface MenuService extends IService<Menu> {


    List<AuthMenuVo> getAuthMenuByRoleId(Integer roleId);

    List<GetMenusByPidVo> getMenusByPid(Integer pid);

    void editMenu(EditMenuDto req);

    PageInfo<GetMenuListVo> menuList(PageDto req);
}
