package com.example.websocketdemo.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.HandshakeHandler;

import java.util.Map;

@Component
public class DoingHandShake implements HandshakeHandler {
    @Override
    public boolean doHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws HandshakeFailureException {
//        System.out.println(request.getAsyncRequestControl(response));
//        System.out.println(response);
//        System.out.println(wsHandler);
        System.out.println(20);
        return false;
    }
}
