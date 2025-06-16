package com.czh.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync  //开启异步
@Configuration
public class ExecutorConfig {

    @Bean(name = "asyncServiceExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程
        executor.setCorePoolSize(10);
        //最大线程
        executor.setMaxPoolSize(20);
        //队列大小
        executor.setQueueCapacity(Integer.MAX_VALUE);
        //线程活跃时间，默认60秒
        //executor.setKeepAliveSeconds();
        //设置线程之间的threadlocal相互传递的方式，自定义一个类去实现TaskDecorator接口
        //executor.setTaskDecorator(MyTaskDecorator);
        //所有任务结束后关闭线程池
        //executor.setWaitForTasksToCompleteOnShutdown(true);
        //设置拒绝策略rejectedExecutionHandler
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //设置线程名称前缀
        executor.setThreadNamePrefix("CZHTaskExecutor-");
        executor.initialize();
        return executor;
    }
}
