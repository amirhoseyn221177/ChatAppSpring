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
        MessageHeaders headers=message.getHeaders();
//        System.out.println(message);
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
