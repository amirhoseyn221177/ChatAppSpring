package com.example.websocketdemo.config;

import com.example.websocketdemo.Repository.PrivateChatRepo;
import com.example.websocketdemo.Services.WebSocketSecurityServices;
import com.example.websocketdemo.Services.PrivateChatServices;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.PrivateChat;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

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
        privateChatServices.savingUserSessions(session);
        privateChatServices.sendingAllQueuedMessagesToUser(session);


    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if(webSocketSecurityServices.isTheMessageAuthorized(session,message)){
            privateChatServices.createQueuesAndExchangeForPrivateChat(message.getPayload());
            privateChatServices.sendingAllQueuedMessagesToUser(session);
            privateChatServices.addMessageToQueue(message.getPayload());
            privateChatServices.saveMessage(message);
            ChatMessage chatMessage=privateChatServices.gettingMessageFromSocket(message);
            List<String> users= new ArrayList<>();
            users.add(chatMessage.getReceiver());
            users.add(chatMessage.getSender());
            Optional<PrivateChat> pr= privateChatRepo.findByUsers(users);
            if(pr.isPresent()){
                PrivateChat privateChat = pr.get();
                System.out.println(privateChat.getMessages());
            }


        }else{
            session.sendMessage(new TextMessage("yoo u ain't authorized"));
        }

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        privateChatServices.deletingUserFromSessions(session);
    }
}
