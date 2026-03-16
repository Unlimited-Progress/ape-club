package com.jingdianjichi.circle.server.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    //    ServerEndpointExporter 是一个 Spring Boot 自动配置类，
//    用于将所有的 @ServerEndpoint 注解的类暴露为 WebSocket 端点。
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
