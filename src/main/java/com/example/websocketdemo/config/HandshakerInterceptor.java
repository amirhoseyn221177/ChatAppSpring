package com.example.websocketdemo.config;


import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
@Component
public class HandshakerInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        if(serverHttpRequest instanceof ServletServerHttpRequest){
            ServletServerHttpRequest servletServerHttpRequest=(ServletServerHttpRequest) serverHttpRequest;
//            System.out.println(19+" "+servletServerHttpRequest.getServletRequest());
//            System.out.println(20+" "+servletServerHttpRequest.getBody());
            HttpSession session =servletServerHttpRequest.getServletRequest().getSession(true);
            if(session!=null){
                map.put("httpSession ",session.getId());
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }
}
