package com.example.websocketdemo.controller;

import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;


@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatServices chatServices;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate, ChatServices chatServices) {
        this.messagingTemplate = messagingTemplate;
        this.chatServices = chatServices;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }


    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String privateUsername = (String) headerAccessor.getSessionAttributes().get("private-username");
        String groupname = (String) headerAccessor.getSessionAttributes().get("groupname");
        System.out.println(username);

        if(username != null) {
            logger.info("User Disconnected : " + username);


            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSender(username);
            chatMessage.setContentType(ChatMessage.ContentType.TEXT);
            chatMessage.setTextContent(username+" "+"has left");

            chatServices.broadCastMessageToGroupChat(chatMessage,groupname);
        }
        
        if(privateUsername != null) {
            logger.info("User Disconnected : " + privateUsername);

            ChatMessage  chatMessage= new ChatMessage();
            chatMessage.setSender(chatMessage.getSender());
            chatMessage.setContentType(ChatMessage.ContentType.TEXT);
            chatMessage.setReceiver((String)headerAccessor.getSessionAttributes().get("private-receiver"));
            chatMessage.setTextContent(chatMessage.getSender()+" "+"has left the chat");

            messagingTemplate.convertAndSend("/queue/reply"+chatMessage.getReceiver(),chatMessage);
        }
    }

  
}
