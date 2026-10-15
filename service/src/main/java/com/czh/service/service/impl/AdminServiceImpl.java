package com.czh.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.common.utils.RedisUtil;
import com.czh.common.vo.SelectVo;
import com.czh.service.cache.AdminAuthCache;
import com.czh.service.cache.AuthCacheKeys;
import com.czh.service.dao.AdminDao;
import com.czh.service.dto.admin.AdminListDto;
import com.czh.service.entity.Admin;
import com.czh.service.service.AdminService;
import com.czh.service.vo.admin.AdminListVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 管理员表(Admin)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:18:32
 */
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminDao,Admin> implements AdminService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Admin getAdminByUsername(String username) {
        return getOne(
                new QueryWrapper<Admin>().eq("userName", username)
        );
    }

    @Override
    public PageInfo<AdminListVo> getAdminList(AdminListDto req) {
        // BE-03：orderBy 是原文拼接进 SQL 的，字段名由后端固定为 adminId，
        // 前端只能决定方向；任何非法值都会被 buildOrderBy 归空（= 不排序）。
        req.setOrderBy(req.buildOrderBy("adminId"));
        PageHelper.startPage(req.getPage(),req.getSize(),req.getOrderBy());
        List<AdminListVo> vos=baseMapper.getAdminList(req);
        return new PageInfo<>(vos);
    }

    @Override
    public List<SelectVo> getAdminSelectList() {
        return baseMapper.getAdminSelectList();
    }

    // ==================== 鉴权缓存 ====================
    // 设计要点：失效动作写在「写方法」里，而不是散落在各个 Controller。
    // 这样任何人调用 adminService.updateById(...) 都不可能忘记清缓存
    // ——忘记清的代价是「管理员被冻结后仍能继续操作系统」这个级别的漏洞。

    @Override
    public AdminAuthCache getAdminCache(Integer adminId) {
        if(adminId == null){
            return null;
        }
        String redisKey = AuthCacheKeys.ADMIN_PREFIX + adminId;
        //1.读缓存
        try {
            String cache = redisUtil.get(redisKey);
            if(cache != null && !cache.isEmpty() && !"null".equals(cache)){
                return MAPPER.readValue(cache, AdminAuthCache.class);
            }
        } catch (Exception e) {
            //缓存损坏或字段不兼容时不能拖垮鉴权主流程，丢弃后回源 DB
            log.warn("管理员缓存解析失败, adminId={}, 回源DB", adminId, e);
        }
        //2.回源 DB
        //  注意：登录时的凭证校验走 getAdminByUsername，始终查库，不吃这份缓存。
        Admin admin = getById(adminId);
        if(admin == null){
            //管理员已不存在：不缓存空值，避免删除操作被延迟生效
            return null;
        }
        AdminAuthCache cache = new AdminAuthCache(
                admin.getAdminId(),
                admin.getUserName(),
                admin.getRoleId(),
                admin.getAvatar(),
                admin.getStatus(),
                admin.getLastTryLoginTime(),
                admin.getFailNum(),
                admin.getLastChangePwdTime()
        );
        //3.回填（刻意不含 password）
        try {
            redisUtil.setEx(redisKey, MAPPER.writeValueAsString(cache),
                    AuthCacheKeys.ADMIN_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("管理员缓存回填失败, adminId={}", adminId, e);
        }
        return cache;
    }

    @Override
    public void evictAdminCache(Integer adminId) {
        if(adminId != null){
            redisUtil.delete(AuthCacheKeys.ADMIN_PREFIX + adminId);
        }
    }

    /**
     * 覆写通用更新：任何管理员数据变更都同步清缓存。
     * <p>
     * 这里<b>不看返回值</b>——MyBatis-Plus 在受影响行数为 0 时返回 false，
     * 而这种情况也可能来自并发写导致行已变化，宁可多查一次 DB 也不留脏缓存。
     */
    @Override
    public boolean updateById(Admin entity) {
        boolean ok = super.updateById(entity);
        evictAdminCache(entity == null ? null : entity.getAdminId());
        return ok;
    }

    @Override
    public boolean update(Admin entity, Wrapper<Admin> updateWrapper) {
        boolean ok = super.update(entity, updateWrapper);
        evictAdminCache(entity == null ? null : entity.getAdminId());
        return ok;
    }

    /**
     * 覆写删除：除了清缓存，还要踢掉登录态，
     * 否则被删除的管理员凭旧 token 仍能操作，直到 Redis 里的 adminToken_ 自然过期。
     */
    @Override
    public boolean removeById(Serializable id) {
        boolean ok = super.removeById(id);
        if(id != null){
            evictAdminCache(Integer.valueOf(String.valueOf(id)));
            redisUtil.delete("adminToken_" + id);
        }
        return ok;
    }
}
