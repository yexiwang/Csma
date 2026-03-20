package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类，用于注册WebSocket的Bean
 */
@Configuration//表明这是一个Web相关的配置类，用于注册web层组件如拦截器、资源处理器等
public class WebSocketConfiguration {

    @Bean//Bean注解表示这个方法返回的对象是一个Bean对象，会被Spring容器管理
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
