package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.service.entity.Area;
import org.apache.ibatis.annotations.Mapper;
@Mapper
/**
 * 地区表(Area)表数据库访问层
 *
 * @author makejava
 * @since 2026-04-04 16:35:00
 */
public interface AreaDao extends BaseMapper<Area> {

}
