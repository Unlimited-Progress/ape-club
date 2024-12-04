package com.jingdianjichi.subject.application.interceptor;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {
//    用于注册一个 RequestInterceptor，以便在每个 Feign 客户端请求中添加自定义的请求头或其他请求信息
    @Bean
    public RequestInterceptor requestInterceptor(){
        return new FeignRequstInterceptor();
    }

}

