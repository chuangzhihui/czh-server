package com.czh.admin;

import com.github.pagehelper.PageHelper;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = {"com.czh"})
@EnableMongoRepositories(basePackages = {"com.czh.service.repository"})
@MapperScan(basePackages = {"com.czh.**.dao"})
public class AdminApplication {
    public static ConfigurableApplicationContext context;
    public static void main(String[] args) {
        context= SpringApplication.run(AdminApplication.class, args);
    }
    @Bean
    PageHelper pageHelper(){
        //分页插件
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("reasonable", "true");

        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("returnPageInfo", "check");
        properties.setProperty("params", "count=countSql");
        pageHelper.setProperties(properties);

        //添加插件
        new SqlSessionFactoryBean().setPlugins(new Interceptor[]{pageHelper});
        return pageHelper;
    }
}
