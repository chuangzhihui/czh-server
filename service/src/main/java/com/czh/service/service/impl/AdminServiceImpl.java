package com.czh.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.common.vo.SelectVo;
import com.czh.service.dao.AdminDao;
import com.czh.service.dto.admin.AdminListDto;
import com.czh.service.entity.Admin;
import com.czh.service.service.AdminService;
import com.czh.service.vo.admin.AdminListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 管理员表(Admin)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:18:32
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminDao,Admin> implements AdminService {



    @Override
    public Admin getAdminByUsername(String username) {
        return getOne(
                new QueryWrapper<Admin>().eq("userName", username)
        );
    }

    @Override
    public PageInfo<AdminListVo> getAdminList(AdminListDto req) {
        if(!req.getOrderBy().isEmpty()){
            req.setOrderBy("adminId "+req.getOrderBy());
        }
        PageHelper.startPage(req.getPage(),req.getSize(),req.getOrderBy());
        List<AdminListVo> vos=baseMapper.getAdminList(req);
        return new PageInfo<>(vos);
    }

    @Override
    public List<SelectVo> getAdminSelectList() {
        return baseMapper.getAdminSelectList();
    }


}
