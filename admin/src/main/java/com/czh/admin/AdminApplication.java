package com.czh.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.czh"})
@MapperScan(basePackages = {"com.czh.**.dao"})
public class AdminApplication {
    public static ConfigurableApplicationContext context;
    public static void main(String[] args) {
        context= SpringApplication.run(AdminApplication.class, args);
    }
}
