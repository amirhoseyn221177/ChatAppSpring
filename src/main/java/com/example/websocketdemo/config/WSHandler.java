package com.example.websocketdemo.config;

import com.example.websocketdemo.Repository.PrivateChatRepo;
import com.example.websocketdemo.Services.WebSocketSecurityServices;
import com.example.websocketdemo.Services.PrivateChatServices;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.PrivateChat;
import com.google.gson.Gson;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class WSHandler extends AbstractWebSocketHandler {
    private final PrivateChatServices privateChatServices;
    private final WebSocketSecurityServices webSocketSecurityServices;
    private final PrivateChatRepo privateChatRepo;
    public WSHandler(PrivateChatServices privateChatServices, WebSocketSecurityServices webSocketSecurityServices, PrivateChatRepo privateChatRepo) {
        this.privateChatServices = privateChatServices;
        this.webSocketSecurityServices = webSocketSecurityServices;
        this.privateChatRepo = privateChatRepo;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        privateChatServices.savingUserSessions(session);
//        privateChatServices.sendingAllQueuedMessagesToUser(session);
        System.out.println(session);



    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        if(webSocketSecurityServices.isTheMessageAuthorized(session,message)){
//            privateChatServices.createQueuesAndExchangeForPrivateChat(message.getPayload());
//            privateChatServices.sendingAllQueuedMessagesToUser(session);
//            privateChatServices.addMessageToQueue(message.getPayload());
//            privateChatServices.saveMessage(message);
//            ChatMessage chatMessage=privateChatServices.gettingMessageFromSocket(message);
//            List<String> users= new ArrayList<>();
//            users.add(chatMessage.getReceiver());
//            users.add(chatMessage.getSender());
//            Optional<PrivateChat> pr= privateChatRepo.findByUsers(users);
//            if(pr.isPresent()){
//                PrivateChat privateChat = pr.get();
//                System.out.println(privateChat.getMessages());
//            }
//        }else{
//            session.sendMessage(new TextMessage("yoo u ain't authorized"));
//        }

        System.out.println(message.getPayload());
        System.out.println(message.getPayload().length());
        session.sendMessage(new TextMessage("salam lil baby"));

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
        if (message.getPayloadLength()>0){
            byte[] messageData = message.getPayload().array();
            Gson gson = new Gson();
            ChatMessage chatMessage = gson.fromJson(new String(messageData),ChatMessage.class);
            System.out.println(chatMessage);
            session.sendMessage(message);
        }
    }



//    @Override
//    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
//        super.handleMessage(session, message);
//        System.out.println(78);
//    }

//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        privateChatServices.deletingUserFromSessions(session);
//    }
}
