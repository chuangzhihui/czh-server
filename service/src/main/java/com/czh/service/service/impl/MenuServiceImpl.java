package com.czh.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.common.dto.PageDto;
import com.czh.common.exception.ErrorException;
import com.czh.common.utils.JSONUtil;
import com.czh.service.dao.MenuDao;
import com.czh.service.dto.admin.EditMenuDto;
import com.czh.service.entity.Menu;
import com.czh.service.entity.Role;
import com.czh.service.service.MenuService;
import com.czh.service.service.RoleService;
import com.czh.service.vo.admin.AuthMenuVo;
import com.czh.service.vo.admin.GetMenuListVo;
import com.czh.service.vo.admin.GetMenusByPidVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * (Menu)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuDao,Menu> implements MenuService {
    @Autowired
    @Lazy
    RoleService roleService;
    @Override
    public List<AuthMenuVo> getAuthMenuByRoleId(Integer roleId) {
        Role role = roleService.getById(roleId);
        if(role==null){
            throw new ErrorException("角色不存在!");
        }
        JSONArray jarr= (JSONArray) JSONArray.parse(role.getIds());
        String ids= JSONUtil.implodeJsonArray(",",jarr);
        // BE-30：一次查全表 + 内存构树，替掉原来的递归 N+1
        // （原实现每个菜单节点各查一次库，而本方法在用户每次刷新页面时都会触发）
        List<Menu> flat = baseMapper.selectAuthMenuFlat(ids);
        return buildAuthMenuTree(flat, 0, "");
    }

    /**
     * 内存构树（BE-30）。
     * <p>
     * 与旧递归实现逐字节等价的关键点：
     * <ul>
     *   <li>只从 {@code pid=0} 起递归，只有挂得上的节点才会出现（孤儿节点行为同旧实现）；</li>
     *   <li>兄弟节点顺序：主保证是 SQL 的 {@code order by sort asc}，方法内另按 sort 升序
     *       做一次内存稳定排序兜底（B-03）——防止后续有人改 SQL 去掉 ORDER BY 时顺序静默漂移；</li>
     *   <li>{@code path} 的拼接规则完全照搬：父路径为空时取自身 path，否则
     *       {@code parentPath + "/" + path}。</li>
     * </ul>
     *
     * @param flat       该角色有权限的全部显示菜单（扁平，已按 sort 升序）
     * @param pid        当前层的父 id
     * @param parentPath 父级已拼好的完整路径
     * @return 当前层的菜单树节点
     */
    private List<AuthMenuVo> buildAuthMenuTree(List<Menu> flat, Integer pid, String parentPath)
    {
        List<AuthMenuVo> menuVos = new ArrayList<>();
        if (flat == null || flat.isEmpty()) {
            return menuVos;
        }
        //B-03：内存排序兜底。复制一份再排，不改调用方传入的列表顺序（避免副作用）。
        List<Menu> ordered=new ArrayList<>(flat);
        ordered.sort(Comparator.comparing(Menu::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        for (Menu menu : ordered) {
            if (!Objects.equals(menu.getPid(), pid)) {
                continue;
            }
            AuthMenuVo vo = new AuthMenuVo();
            vo.setId(menu.getId());
            vo.setTitle(menu.getName());
            vo.setIcon(menu.getIcon());
            String fullPath = (parentPath == null || parentPath.isEmpty())
                    ? menu.getPath()
                    : parentPath + "/" + menu.getPath();
            vo.setPath(fullPath);
            vo.setChild(buildAuthMenuTree(flat, menu.getId(), fullPath));
            menuVos.add(vo);
        }
        return menuVos;
    }

    @Override
    public List<GetMenusByPidVo> getMenusByPid(Integer pid) {
        return baseMapper.getMenusByPid(pid);
    }

    @Override
    public void editMenu(EditMenuDto req) {
        Menu menu=getById(req.getId());
        if(menu==null){
            throw new ErrorException("菜单不存在!");
        }
        //判断菜单等级
        if(req.getLevel()>1){
            List<GetMenusByPidVo> vos=getMenusByPid(req.getId());
            if(!vos.isEmpty()){//有一层了
                if(req.getLevel()==3){
                   throw new ErrorException("菜单下层级超过三层!");
                }
                if(req.getLevel()==2){
                    for(GetMenusByPidVo vo:vos){
                        List<GetMenusByPidVo> vos1=getMenusByPid(vo.getId());
                        if(!vos1.isEmpty()){
                            //有两层了
                            throw new ErrorException("菜单下层级超过三层!");
                        }
                    }
                }
            }
        }
        if(req.getDisplay() == 1
                && (req.getPath() == null || req.getPath().trim().isEmpty())){
            throw new ErrorException("显示菜单必须配置前端路由(path)");
        }
        if(req.getPath() != null && !req.getPath().matches("[a-zA-Z0-9_-]+(/[a-zA-Z0-9_-]+)*")){
            throw new ErrorException("前端路由格式不正确：仅允许字母、数字、-、_，多段用 / 分隔（段内不能为空）");
        }
        BeanUtils.copyProperties(req,menu);
        updateById(menu);
    }

    @Override
    public PageInfo<GetMenuListVo> menuList(PageDto req) {
        PageHelper.startPage(req.getPage(),req.getSize(),"sort asc");
        List<GetMenuListVo>vos=getMenuListByPid(0);
        return new PageInfo<>(vos);
    }
    private  List<GetMenuListVo> getMenuListByPid(Integer pid)
    {
        List<GetMenuListVo>vos=baseMapper.getMenuListByPid(pid);
        for (GetMenuListVo vo:vos) {
            vo.setChild(getMenuListByPid(vo.getId()));
        }
        return vos;
    }

    // ==================== 写操作：维护角色权限缓存 ====================
    // 角色的权限串取自 menu.route，所以菜单一旦变了，引用它的角色的缓存就脏了。
    // 但从缓存侧无法反查「哪些角色引用了这个菜单」（ids 是存在 role 表里的 JSON 数组），
    // 因此走全量失效。菜单是低频写，代价可以忽略。

    /**
     * 覆写更新：<b>仅当 route 真的变了</b>才全量清理。
     * <p>
     * MyBatis-Plus 的 updateById 只更新非 null 字段，所以 entity.getRoute() == null
     * 表示本次没改路由（改名称、改排序、改图标），不必惊动权限缓存。
     */
    @Override
    public boolean updateById(Menu entity) {
        Menu before = (entity != null && entity.getRoute() != null) ? getById(entity.getId()) : null;
        boolean ok = super.updateById(entity);
        if(before != null && !Objects.equals(before.getRoute(), entity.getRoute())){
            roleService.evictAllRolePermissionCache();
        }
        return ok;
    }

    @Override
    public boolean update(Menu entity, Wrapper<Menu> updateWrapper) {
        boolean ok = super.update(entity, updateWrapper);
        roleService.evictAllRolePermissionCache();
        return ok;
    }

    /**
     * 覆写删除：菜单删除后，角色 ids 里残留的 id 会对应不上，
     * 该角色的权限串必须重算（否则继续按已删除的路由放行）。
     */
    @Override
    public boolean removeById(Serializable id) {
        boolean ok = super.removeById(id);
        roleService.evictAllRolePermissionCache();
        return ok;
    }
}
