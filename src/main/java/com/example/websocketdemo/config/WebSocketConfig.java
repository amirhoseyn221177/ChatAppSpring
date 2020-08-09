package com.example.websocketdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeHandler;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final handShakerInterceptor handshakerInterceptor;
    private final ChannelInterceptor inboundMessageChannelInterceptor;
    private final HandshakeHandler DoingHandShake;

    public WebSocketConfig(handShakerInterceptor handshakerInterceptor, ChannelInterceptor inboundMessageChannelInterceptor, HandshakeHandler doingHandShake) {
        this.handshakerInterceptor = handshakerInterceptor;
        this.inboundMessageChannelInterceptor = inboundMessageChannelInterceptor;
        DoingHandShake = doingHandShake;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*").addInterceptors(handshakerInterceptor);

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

        //   Use this for enabling a Full featured broker like RabbitMQ or ActiveMQ

        registry.setApplicationDestinationPrefixes("/app","/user")
                .enableStompBrokerRelay("/topic/**","/fanout/**","/direct/**","/queue/device")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");

//        registry.setApplicationDestinationPrefixes("/app","/user");
//        registry.enableSimpleBroker("/queue/", "/topic/")
//                .setTaskScheduler(new DefaultManagedTaskScheduler())
//                .setHeartbeatValue(new long[]{10000,10000});  //first one is how often server will write second how often client will write
        // Enables a simple in-memory broker

    }


}
