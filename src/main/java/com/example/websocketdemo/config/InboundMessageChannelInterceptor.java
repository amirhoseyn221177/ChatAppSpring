package com.example.websocketdemo.config;

import com.example.websocketdemo.Security.TokenValidator;
import com.example.websocketdemo.Services.CustomUserServices;
import com.example.websocketdemo.model.ChatUser;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class InboundMessageChannelInterceptor implements ChannelInterceptor {

    private final TokenValidator tokenValidator;
    private final CustomUserServices customUserServices;

    public InboundMessageChannelInterceptor(TokenValidator tokenValidator, CustomUserServices customUserServices) {
        this.tokenValidator = tokenValidator;
        this.customUserServices = customUserServices;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel) {
        StompHeaderAccessor stompHeaderAccessor= MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        assert stompHeaderAccessor != null;
        if(StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())){
            List<String> authorization = stompHeaderAccessor.getNativeHeader("Authorization");
            System.out.println(authorization);
//            assert authorization != null;
//            String accessToken=authorization.get(0).split(" ")[1];
//            Jwt jwt= jwtDecoder.decode(accessToken);
//            JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
//            Authentication authentication=jwtAuthenticationConverter.convert(jwt);
//            stompHeaderAccessor.setUser(authentication);
            assert authorization != null;
            String bearerToken = authorization.get(0);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("bearer")) {
                String token = bearerToken.substring(7);
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                System.out.println(authentication);
                Principal principal = (Principal) authentication.getPrincipal();
                stompHeaderAccessor.setUser(principal);
            }
        }
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel messageChannel, boolean b) {
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel messageChannel, boolean b, Exception e) {
    }

    @Override
    public boolean preReceive(MessageChannel messageChannel) {
        return false;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel messageChannel) {
        System.out.println(35);
        return message;
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel messageChannel, Exception e) {
        System.out.println(41);
    }
}
