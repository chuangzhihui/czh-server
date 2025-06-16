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
        if(!req.getOrderBy().isEmpty()){
            req.setOrderBy("id "+req.getOrderBy());
        }
        PageHelper.startPage(req.getPage(),req.getSize(),req.getOrderBy());
        List<GetSettingListVo> vos= baseMapper.getSettingList();
        return new PageInfo<>(vos);
    }
}
