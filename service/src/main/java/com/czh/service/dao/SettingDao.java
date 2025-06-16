package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.service.entity.Setting;
import com.czh.service.vo.admin.GetSettingListVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
/**
 * 系统设置表(Setting)表数据库访问层
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface SettingDao extends BaseMapper<Setting> {

    List<GetSettingListVo> getSettingList();
}
