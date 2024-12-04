package com.jingdianjichi.club.gateway.exception;

import cn.dev33.satoken.exception.SaTokenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingdianjichi.club.gateway.entity.Result;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关全局异常处理
 *
 * @author: ChickenWing
 * @date: 2023/10/28
 */
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private ObjectMapper objectMapper = new ObjectMapper();

    //serverWebExchange处理关于request这种，throwable是网关截取到的异常
    @Override
//    Mono<Void> 常用于表示那些只关注操作完成与否、而不需要处理返回值的场景。
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        ServerHttpResponse response = serverWebExchange.getResponse();
        Integer code=200;
        String message ="";
//        instanceof是Java中的一个关键字，用于检查一个对象是否属于某个类或接口的实例
        if (throwable instanceof SaTokenException){
            code =401;
            message = "用户无权限";
        }else {
            code =500;
            message = "系统繁忙";
        }
        Result result = Result.fail(code, message);

// 设置响应头的内容类型为JSON
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
// 使用Mono.fromSupplier创建一个异步的响应流，并将结果写入响应体
        return response.writeWith(Mono.fromSupplier(() -> {
            // 获取响应的缓冲区工厂
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            byte[] bytes = null;
            try {
                // 将result对象转换为JSON字节数组
                bytes = objectMapper.writeValueAsBytes(result);
            } catch (JsonProcessingException e) {
                // 如果转换过程中出现异常，打印异常堆栈信息
                e.printStackTrace();
            }
            // 使用缓冲区工厂包装字节数组，并返回包装后的数据缓冲区
            return dataBufferFactory.wrap(bytes);
        }));

    }
}
