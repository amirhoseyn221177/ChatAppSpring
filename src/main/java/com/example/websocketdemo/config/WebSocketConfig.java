package com.example.websocketdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final HandshakerInterceptor handshakerInterceptor;

    public WebSocketConfig(HandshakerInterceptor handshakerInterceptor) {
        this.handshakerInterceptor = handshakerInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS().setInterceptors(handshakerInterceptor);
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
