package com.example.websocketdemo.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurity extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    public void configureInbound(MessageSecurityMetadataSourceRegistry message){
        message
                .simpDestMatchers("/ws/**").authenticated()
                .anyMessage().authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
