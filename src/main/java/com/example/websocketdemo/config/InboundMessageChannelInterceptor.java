package com.example.websocketdemo.config;

import com.example.websocketdemo.Security.TokenValidator;
import com.example.websocketdemo.Services.CustomUserServices;
import com.example.websocketdemo.model.ChatUser;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Component
public class InboundMessageChannelInterceptor implements ChannelInterceptor {

    private final TokenValidator tokenValidator;
    private final CustomUserServices customUserServices;

    public InboundMessageChannelInterceptor(TokenValidator tokenValidator, CustomUserServices customUserServices) {
        this.tokenValidator = tokenValidator;
        this.customUserServices = customUserServices;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel) {
        StompHeaderAccessor stompHeaderAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        try {
            assert stompHeaderAccessor != null;
            if (StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())) {
                List<String> authorization = stompHeaderAccessor.getNativeHeader("Authorization");
                assert authorization != null;
                String bearerToken = authorization.get(0);
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("bearer")) {
                    String token = bearerToken.substring(7);
                    String userId = tokenValidator.GetIdFromToken(token);
                    ChatUser chatUser = customUserServices.loadByID(userId);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            chatUser, null, Collections.emptyList()
                    );
                    System.out.println("we are in pre send ");
                    stompHeaderAccessor.setUser(authenticationToken);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        System.out.println(73);
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
