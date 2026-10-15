package com.czh.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.czh.common.dto.PageDto;
import com.czh.service.entity.Setting;
import com.czh.service.vo.admin.GetSettingListVo;
import com.github.pagehelper.PageInfo;

/**
 * 系统设置表(Setting)表服务接口
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface SettingService extends IService<Setting> {


    PageInfo<GetSettingListVo> getSettingList(PageDto req);
}
