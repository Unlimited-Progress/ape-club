package com.jingdianjichi.subject.domain.config;

import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池的config管理
 * 为什么用自定义的线程池：1.自由灵活 2. 我们去做队列的时候可以指定队列，这样不会产生堆栈的溢出
 */
@Configuration
public class ThreadPoolConfig {

//
//    @Value("${thread.pool.corePoolSize}")
//    private int corePoolSize;
//
//    @Value("${thread.pool.maxPoolSize}")
//    private int maxPoolSize;
//
//    @Value("${thread.pool.queueCapacity}")
//    private int queueCapacity;
//
//    @Value("${thread.pool.keepAliveSeconds}")


    @Bean(name = "labelThreadPool")
    public ThreadPoolExecutor getLabelThreadPool(){
        return new ThreadPoolExecutor(20,100,5,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(40),
                new CustomNameThreadFactory("label"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

}
