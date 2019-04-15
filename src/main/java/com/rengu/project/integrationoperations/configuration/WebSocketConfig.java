package com.rengu.project.integrationoperations.configuration;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/12 12:56
 */

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Component
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
