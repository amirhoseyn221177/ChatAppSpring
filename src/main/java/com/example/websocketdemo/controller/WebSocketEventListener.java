package com.example.websocketdemo.controller;

import com.example.websocketdemo.Services.PrivateChatServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import org.springframework.messaging.simp.SimpMessageSendingOperations;

import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;


//@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final PrivateChatServices privateChatServices;


    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate, PrivateChatServices privateChatServices) {
        this.messagingTemplate = messagingTemplate;
        this.privateChatServices = privateChatServices;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");

    }


    @EventListener
    public void handleWebsocketLeaveGroup(SessionUnsubscribeEvent event){
//        StompHeaderAccessor headerAccessor=StompHeaderAccessor.wrap(event.getMessage());
//        String username =(String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
//        System.out.println(username);
    }

    @EventListener
    public void handleWebsocketSubscribed(SessionSubscribeEvent event){
//        StompHeaderAccessor headerAccessor =StompHeaderAccessor.wrap(event.getMessage());
//        String username =  headerAccessor.getFirstNativeHeader("user");
//        System.out.println(username);

    }
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        String username = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
//        String privateUsername = (String) headerAccessor.getSessionAttributes().get("private-username");
//        String groupname =  headerAccessor.getFirstNativeHeader("groupname");
//        System.out.println("we are in handle disconnect");
//        if(username != null) {
//            logger.info("User Disconnected : " + username);
//
//
//            ChatMessage chatMessage = new ChatMessage();
//            chatMessage.setSender(username);
//            chatMessage.setContentType("text");
//            chatMessage.setTextContent(username+" "+"has left the chat");
//
////            chatServices.broadCastMessageToGroupChat(chatMessage,groupname);
//        }
//
//        if(privateUsername != null) {
//            logger.info("User Disconnected : " + privateUsername);
//
//        }
    }

  
}
