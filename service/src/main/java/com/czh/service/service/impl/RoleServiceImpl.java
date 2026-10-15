package com.czh.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.common.dto.PageDto;
import com.czh.common.exception.ErrorException;
import com.czh.common.utils.RedisUtil;
import com.czh.common.vo.SelectVo;
import com.czh.service.cache.AuthCacheKeys;
import com.czh.service.dao.RoleDao;
import com.czh.service.entity.Menu;
import com.czh.service.entity.Role;
import com.czh.service.service.MenuService;
import com.czh.service.service.RoleService;
import com.czh.service.vo.admin.AddRoleGetMenusVo;
import com.czh.service.vo.admin.GetMenusByPidVo;
import com.czh.service.vo.admin.RoleListVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


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
    public PageInfo<RoleListVo> getRoleList(PageDto req) {
        // BE-03：同上，字段名后端固定为 roleId，前端只能决定方向
        req.setOrderBy(req.buildOrderBy("roleId"));
        PageHelper.startPage(req.getPage(),req.getSize(),req.getOrderBy());
        List<Role> roles=list();
        //实体转 VO，避免把 Role 实体（含后续新增字段）直接暴露给接口
        List<RoleListVo> vos=new ArrayList<>();
        for(Role role:roles){
            RoleListVo vo=new RoleListVo();
            BeanUtils.copyProperties(role,vo);
            vos.add(vo);
        }
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
       String redisKey= AuthCacheKeys.ROLE_PERMISSION_PREFIX +role.getRoleId();
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
           redisUtil.setEx(redisKey,cache, AuthCacheKeys.ROLE_PERMISSION_TTL_SECONDS, TimeUnit.SECONDS);
           //登记到索引集合：菜单 route 变更时要按此枚举做全量失效
           redisUtil.sAdd(AuthCacheKeys.ROLE_PERMISSION_INDEX, String.valueOf(role.getRoleId()));
       }
        return permissions;
    }
    //更新角色权限缓存
    @Override
    public void updateRoleCache(Integer roleId) {
        String redisKey=AuthCacheKeys.ROLE_PERMISSION_PREFIX +roleId;
        redisUtil.delete(redisKey);
        getRolePermissions(roleId);
    }

    /**
     * 全量失效角色权限缓存。
     * <p>
     * 走索引集合而不是 {@code keys rolePermission*}：Redis 的 keys 是全库 O(N) 扫描，
     * 会阻塞单线程主线程，线上是禁止使用的命令。
     */
    @Override
    public void evictAllRolePermissionCache() {
        Set<String> roleIds = redisUtil.setMembers(AuthCacheKeys.ROLE_PERMISSION_INDEX);
        if(roleIds != null && !roleIds.isEmpty()){
            List<String> keys = new ArrayList<>();
            for(String roleId : roleIds){
                keys.add(AuthCacheKeys.ROLE_PERMISSION_PREFIX + roleId);
            }
            redisUtil.delete(keys);
            log.info("菜单数据变更,已清理角色权限缓存 roleCount={}", roleIds.size());
        }
        redisUtil.delete(AuthCacheKeys.ROLE_PERMISSION_INDEX);
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
