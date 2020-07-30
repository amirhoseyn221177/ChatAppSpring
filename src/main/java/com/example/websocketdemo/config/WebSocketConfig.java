package com.example.websocketdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final HandshakerInterceptor handshakerInterceptor;
    private final InboundMessageChannelInterceptor inboundMessageChannelInterceptor;
    public WebSocketConfig(HandshakerInterceptor handshakerInterceptor, InboundMessageChannelInterceptor inboundMessageChannelInterceptor) {
        this.handshakerInterceptor = handshakerInterceptor;
        this.inboundMessageChannelInterceptor = inboundMessageChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS().setInterceptors(handshakerInterceptor);

    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(20000000);
        registry.setSendBufferSizeLimit(3*512*1024);
        registry.setSendTimeLimit(20*10000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(inboundMessageChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app","/user");
        registry.enableSimpleBroker("/queue/", "/topic/");   // Enables a simple in-memory broker
//        registry.setUserDestinationPrefix("/user");



        //   Use this for enabling a Full featured broker like RabbitMQ or ActiveMQ

        /*
        registry.enableStompBrokerRelay("/topic")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");
        */
    }


}
