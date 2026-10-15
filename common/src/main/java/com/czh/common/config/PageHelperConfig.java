package com.czh.common.config;

import com.github.pagehelper.PageHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 分页插件配置（PageHelper）
 * <p>
 * 放在 common 模块，由 admin / api 共用，避免各启动类重复声明。
 * PageHelper 实现了 MyBatis 的 {@code Interceptor}，注册为 Bean 后会被
 * MyBatis 自动配置装配进 SqlSessionFactory，无需手动 setPlugins。
 * <p>
 * 生效前提：启动类 {@code @ComponentScan} 覆盖 {@code com.czh}
 * （AdminApplication / ApiApplication 均已覆盖）。
 */
@Configuration
public class PageHelperConfig {

    /**
     * 分页插件
     */
    @Bean
    public PageHelper pageHelper() {
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        // 分页参数合理化：pageNum<=0 时查第一页，pageNum>总页数时查最后一页
        properties.setProperty("reasonable", "true");
        // 支持通过 Mapper 接口参数传递分页参数
        properties.setProperty("supportMethodsArguments", "true");
        // 返回值类型为 PageInfo 时自动分页（check 表示按需判断）
        properties.setProperty("returnPageInfo", "check");
        // 用 countSql 作为 count 查询
        properties.setProperty("params", "count=countSql");
        pageHelper.setProperties(properties);
        return pageHelper;
    }
}
