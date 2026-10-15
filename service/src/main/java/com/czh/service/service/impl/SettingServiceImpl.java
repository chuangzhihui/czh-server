package com.czh.service.service.impl;

import com.czh.common.dto.PageDto;
import com.czh.service.entity.Setting;
import com.czh.service.dao.SettingDao;
import com.czh.service.service.SettingService;
import com.czh.service.vo.admin.GetSettingListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;


/**
 * 系统设置表(Setting)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Service
public class SettingServiceImpl extends ServiceImpl<SettingDao,Setting> implements SettingService {

    @Override
    public PageInfo<GetSettingListVo> getSettingList(PageDto req) {
        // BE-03：与 AdminServiceImpl / RoleServiceImpl 统一走白名单出口。
        // 这是评估报告 BE-03 未列到的第四个调用点（同构写法），一并收敛。
        req.setOrderBy(req.buildOrderBy("id"));
        PageHelper.startPage(req.getPage(),req.getSize(),req.getOrderBy());
        List<GetSettingListVo> vos= baseMapper.getSettingList();
        return new PageInfo<>(vos);
    }
}
