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
        //队列大小：必须有界。原为 Integer.MAX_VALUE，高负载下会无限堆积导致 OOM，
        //且队列永不满会使下面的拒绝策略形同虚设。
        executor.setQueueCapacity(500);
        //线程活跃时间，默认60秒
        //executor.setKeepAliveSeconds();
        //设置线程之间的threadlocal相互传递的方式，自定义一个类去实现TaskDecorator接口
        //executor.setTaskDecorator(MyTaskDecorator);
        //所有任务结束后关闭线程池（优雅停机，避免重启时丢失正在写入的日志/异步任务）
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //等待任务完成的超时时间（秒）
        executor.setAwaitTerminationSeconds(30);
        //设置拒绝策略rejectedExecutionHandler：队列满后交由调用方线程执行，形成天然背压
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //设置线程名称前缀
        executor.setThreadNamePrefix("CZHTaskExecutor-");
        executor.initialize();
        return executor;
    }
}
