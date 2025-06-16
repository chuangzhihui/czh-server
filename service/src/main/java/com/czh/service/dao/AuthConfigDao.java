package com.czh.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.czh.service.entity.AuthConfig;
import org.apache.ibatis.annotations.Mapper;
@Mapper
/**
 * 登录安全配置(AuthConfig)表数据库访问层
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
public interface AuthConfigDao extends BaseMapper<AuthConfig> {

}
