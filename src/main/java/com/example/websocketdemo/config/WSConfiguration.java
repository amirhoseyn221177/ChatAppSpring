package com.example.websocketdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WSConfiguration implements WebSocketConfigurer {
    private final WSHandler wsHandler;
    private final handShakerInterceptor handShakerInterceptor;
    private final DoingHandShake doingHandShake;
    public WSConfiguration(WSHandler wsHandler, com.example.websocketdemo.config.handShakerInterceptor handShakerInterceptor,
                           DoingHandShake doingHandShake ) {
        this.wsHandler = wsHandler;
        this.handShakerInterceptor = handShakerInterceptor;
        this.doingHandShake = doingHandShake;
    }

// one good idea is that we let the socket gets connected but till its getting authorized it is not allowed to do anthything
    // at all and we can do it in map in handShakerInterceptor 
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsHandler,"/ws/**").setAllowedOrigins("*")
                .setHandshakeHandler(doingHandShake)
                .addInterceptors(handShakerInterceptor);

    }
}
