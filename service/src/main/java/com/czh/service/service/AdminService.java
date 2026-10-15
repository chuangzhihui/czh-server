package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.common.vo.SelectVo;
import com.czh.service.cache.AdminAuthCache;
import com.czh.service.dto.admin.AdminListDto;
import com.czh.service.entity.Admin;
import com.czh.service.vo.admin.AdminListVo;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 管理员表(Admin)表服务接口
 *
 * @author makejava
 * @since 2025-06-05 22:18:32
 */
public interface AdminService extends IService<Admin> {
    Admin getAdminByUsername(String username);


    PageInfo<AdminListVo> getAdminList(AdminListDto req);

    List<SelectVo> getAdminSelectList();

    /**
     * 获取管理员鉴权缓存（缓存未命中时回源 DB 并回填）。
     * <p>
     * 只用于请求鉴权链路，<b>不含密码</b>；登录时的凭证校验请走 {@link #getAdminByUsername(String)}。
     *
     * @param adminId 管理员 id
     * @return 缓存对象；管理员不存在时返回 null
     */
    AdminAuthCache getAdminCache(Integer adminId);

    /**
     * 删除指定管理员的鉴权缓存。
     * <p>
     * 正常不需要外部调用：本类的写方法（updateById / update / removeById）已自动失效。
     * 仅在绕过 Service 直接改库的场景手动补偿。
     *
     * @param adminId 管理员 id
     */
    void evictAdminCache(Integer adminId);
}
