package com.example.websocketdemo.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class InboundMessageChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel) {
        StompHeaderAccessor stompHeaderAccessor= StompHeaderAccessor.wrap(message);
        MessageHeaders headers=message.getHeaders();
        stompHeaderAccessor.setMessage("i need kos");
        System.out.println(12);
        System.out.println(message.getHeaders());
        System.out.println(message.getPayload());
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel messageChannel, boolean b) {
        StompHeaderAccessor stompHeaderAccessor= StompHeaderAccessor.wrap(message);
        MessageHeaders headers=message.getHeaders();

        System.out.println(24);
        System.out.println(headers);
        System.out.println(stompHeaderAccessor);

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
        StompHeaderAccessor stompHeaderAccessor= StompHeaderAccessor.wrap(message);
        MessageHeaders headers=message.getHeaders();
        System.out.println(37);
        System.out.println(headers);
        System.out.println(stompHeaderAccessor);
        return message;
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel messageChannel, Exception e) {
        StompHeaderAccessor stompHeaderAccessor= StompHeaderAccessor.wrap(message);
        MessageHeaders headers=message.getHeaders();
        System.out.println(49);
        System.out.println(headers);
        System.out.println(stompHeaderAccessor);
    }
}
