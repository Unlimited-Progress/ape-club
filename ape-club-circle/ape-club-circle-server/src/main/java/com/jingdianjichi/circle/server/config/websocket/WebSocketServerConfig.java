package com.jingdianjichi.circle.server.config.websocket;

import com.alibaba.nacos.common.utils.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

@Component
//用于配置 WebSocket 端点的行为。这个类主要用于处理跨域请求和握手过程中的自定义逻辑
public class WebSocketServerConfig extends ServerEndpointConfig.Configurator {


    //    作用：这个方法用于检查 WebSocket 请求的来源（Origin）是否允许。
//    用途：通常用于处理跨域请求，允许或拒绝来自特定域名的 WebSocket 连接。
    @Override
    //    originHeaderValue：客户端请求中的 Origin 头的值。
    public boolean checkOrigin(String originHeaderValue) {
        //    返回值：true 表示允许任何来源的请求，false 表示拒绝请求。
        return true;
//        return originHeaderValue.startsWith("http://localhost") || originHeaderValue.startsWith("https://yourdomain.com");

    }

    //    作用：这个方法在 WebSocket 握手过程中被调用，用于修改握手请求和响应。
    @Override
//    sec：ServerEndpointConfig 对象，包含当前端点的配置信息。
//    request：HandshakeRequest 对象，包含客户端发起握手请求的信息。
//    response：HandshakeResponse 对象，用于设置握手响应的信息。
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        //获取握手参数
        Map<String, List<String>> parameterMap = request.getParameterMap();

        List<String> erpList = parameterMap.get("erp");
        if(!CollectionUtils.isEmpty(erpList)){
            sec.getUserProperties().put("erp", erpList.get(0));
        }
    }

}
