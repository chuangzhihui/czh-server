package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.common.vo.SelectVo;
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
}
