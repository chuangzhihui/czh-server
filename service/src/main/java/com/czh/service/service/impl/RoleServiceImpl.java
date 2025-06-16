package com.czh.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.common.dto.PageDto;
import com.czh.common.exception.ErrorException;
import com.czh.common.utils.RedisUtil;
import com.czh.common.vo.SelectVo;
import com.czh.service.dao.RoleDao;
import com.czh.service.entity.Menu;
import com.czh.service.entity.Role;
import com.czh.service.service.MenuService;
import com.czh.service.service.RoleService;
import com.czh.service.vo.admin.AddRoleGetMenusVo;
import com.czh.service.vo.admin.GetMenusByPidVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * 管理员角色表(Role)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Service
@Slf4j
public class RoleServiceImpl extends ServiceImpl<RoleDao,Role> implements RoleService {
    @Autowired
    @Lazy
    MenuService menuService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public PageInfo<Role> getRoleList(PageDto req) {
        if(!req.getOrderBy().isEmpty()){
            req.setOrderBy("roleId "+req.getOrderBy());
        }
        PageHelper.startPage(req.getPage(),req.getSize(),req.getOrderBy());
        List<Role> vos=list();
        return new PageInfo<>(vos);
    }

    @Override
    public List<AddRoleGetMenusVo> getAddRoleMenus() {
        List<GetMenusByPidVo>menus= menuService.getMenusByPid(0);
        return getSons(menus);
    }

    @Override
    public List<SelectVo> getRoleSelectList() {
        return baseMapper.getRoleSelectList();
    }

    @Override
    public List<String> getRolePermissions(Integer roleId) {
       Role role=getById(roleId);
       if(role==null){
           throw new ErrorException("角色不存在!");
       }
       String redisKey="rolePermission"+role.getRoleId();
        List<String> permissions=new ArrayList<>();
        String rolePermissions=redisUtil.get(redisKey);
       if(redisUtil.hasKey(redisKey) && rolePermissions!=null && !rolePermissions.isEmpty()){
           permissions= JSON.parseArray(rolePermissions,String.class);
       }else{
           ObjectMapper mapper = new ObjectMapper();
           List<Integer> ids= new ArrayList<>();
           try {
               ids = mapper.readValue(role.getIds(),new TypeReference<List<Integer>>() {});
           } catch (JsonProcessingException e) {

           }
           List<Menu> menus=menuService.list(
                   new QueryWrapper<Menu>()
                           .in("id",ids)
           );

           for(Menu menu:menus){
               if(menu.getRoute()!= null && !menu.getRoute().isEmpty()){
                   permissions.add(menu.getRoute());
               }
           }
           String cache=JSON.toJSONString(permissions);
           redisUtil.set(redisKey,cache);
       }
        return permissions;
    }
    //更新角色权限缓存
    @Override
    public void updateRoleCache(Integer roleId) {
        String redisKey="rolePermission"+roleId;
        redisUtil.delete(redisKey);
        getRolePermissions(roleId);
    }
    private List<AddRoleGetMenusVo> getSons(List<GetMenusByPidVo>menus){
        List<AddRoleGetMenusVo>vos=new LinkedList<>();
        for(GetMenusByPidVo vo:menus){
            AddRoleGetMenusVo v=new AddRoleGetMenusVo();
            v.setId(vo.getId());
            v.setName(vo.getName());
            List<GetMenusByPidVo>menus1= menuService.getMenusByPid(vo.getId());
            v.setChild(getSons(menus1));
            vos.add(v);
        }
        return vos;
    }

}
