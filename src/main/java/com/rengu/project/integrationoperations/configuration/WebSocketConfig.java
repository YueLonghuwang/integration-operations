package com.rengu.project.integrationoperations.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * author : yaojiahao
 * Date: 2019/7/23 10:41
 **/
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/receiveHeartbeatCMD", "/uploadHeartBeatMessage", "/uploadSelfInspectionResult","uploadSoftwareVersionMessage","uploadDeviceNetWorkParamMessage","uploadRadarSubSystemWorkStatusMessage");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/INTEGRATION").setAllowedOrigins("*").withSockJS();
    }
}
