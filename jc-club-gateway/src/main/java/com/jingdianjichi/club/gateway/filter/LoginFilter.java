package com.jingdianjichi.club.gateway.filter;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoginFilter implements GlobalFilter {


    @Override
    @SneakyThrows
//    Mono<Void> 常用于表示那些只关注操作完成与否、而不需要处理返回值的场景。
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 创建一个新的请求构建器，用于修改请求
        ServerHttpRequest.Builder mutate = request.mutate();
        String url = request.getURI().getPath();
        log.info("LoginFilter.filter.url:{}", url);
        if (url.equals("/user/doLogin")) {
//            继续执行过滤器链
            chain.filter(exchange);
        }

        try {
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            log.info("LoginFilter.filter.url:{}", new Gson().toJson(tokenInfo));
            if (tokenInfo == null || StringUtils.isEmpty(tokenInfo.getLoginId())) {
                // 返回未授权响应，而不是抛出异常
                return Mono.error(new RuntimeException("未获取到用户信息"));
            }

            String loginId = (String) tokenInfo.getLoginId();

            mutate.header("loginId", loginId);
            // 使用修改后的请求继续过滤链的处理
            return chain.filter(exchange.mutate().request(mutate.build()).build());
        } catch (Exception e) {
            log.error("Error in LoginFilter", e);
            return Mono.error(e);
        }
    }
}
