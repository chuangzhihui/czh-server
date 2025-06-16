package com.czh.service.service.impl;

import com.alibaba.fastjson.JSONArray;
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

import java.util.List;


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
        return getAuthMenuList(0,ids);
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

    private List<AuthMenuVo> getAuthMenuList(Integer pid,String ids)
    {
        List<AuthMenuVo> menuVos=baseMapper.getAuthMenuList(pid,ids);
        for(AuthMenuVo authMenuVo:menuVos){
            authMenuVo.setChild(getAuthMenuList(authMenuVo.getId(),ids));
        }
        return menuVos;
    }
}
