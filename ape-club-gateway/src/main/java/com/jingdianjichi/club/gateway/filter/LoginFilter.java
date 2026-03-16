package com.jingdianjichi.club.gateway.filter;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 登录认证过滤器
 * 
 * 该类实现了Spring Cloud Gateway的GlobalFilter接口，作为全局过滤器用于处理所有请求的登录认证。
 * 它通过Sa-Token框架验证用户身份，并将登录用户信息添加到请求头中，供下游服务使用。
 * 
 * @author 系统生成
 * @date 2023/11/01
 */
@Component
@Slf4j
public class LoginFilter implements GlobalFilter {

    /**
     * 过滤器核心处理方法
     * 
     * 该方法对所有经过网关的请求进行拦截处理，主要功能包括：
     * 1. 记录请求路径信息
     * 2. 对登录请求(/user/doLogin)直接放行
     * 3. 验证其他请求的Sa-Token信息
     * 4. 将登录用户ID添加到请求头中，传递给下游服务
     * 
     * @param exchange 服务器Web交换对象，包含请求和响应信息
     * @param chain 过滤器链，用于继续执行后续过滤器
     * @return Mono<Void> 异步响应对象，表示过滤器处理完成
     * @throws RuntimeException 当用户未登录或Token无效时抛出
     */
    @Override
    @SneakyThrows
    // Mono<Void> 常用于表示那些只关注操作完成与否、而不需要处理返回值的场景。
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取当前请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 创建一个新的请求构建器，用于修改请求头信息
        ServerHttpRequest.Builder mutate = request.mutate();
        // 获取请求路径
        String url = request.getURI().getPath();
        // 记录请求路径，便于调试和监控
        log.info("LoginFilter.filter.url:{}", url);
        
        // 对登录请求直接放行，不需要验证Token
        if (url.equals("/user/doLogin")) {
            // 继续执行过滤器链，不进行任何修改
            return chain.filter(exchange);
        }

        try {
            // 优先透传前端已携带的 loginId，避免在响应式网关里直接访问线程上下文
            String loginId = request.getHeaders().getFirst("loginId");
            if (StringUtils.isEmpty(loginId)) {
                return chain.filter(exchange);
            }

            // 将登录用户ID添加到请求头中，供下游服务使用
            mutate.header("loginId", loginId);
            
            // 使用修改后的请求继续过滤链的处理
            return chain.filter(exchange.mutate().request(mutate.build()).build());
        } catch (Exception e) {
            // 捕获并记录异常信息
            log.error("Error in LoginFilter", e);
            // 将异常传播到错误处理器
            return Mono.error(e);
        }
    }
}
