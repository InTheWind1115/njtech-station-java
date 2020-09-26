package com.njtechstation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author Liucheng
 * @function 配置类 @ServerEndPoint 使用websocket必备
 */
@Configuration
public class ServerEndpointConfigurator {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
