package com.example.websocketdemo.config;

import com.example.websocketdemo.Services.WebSocketSecurityServices;
import com.example.websocketdemo.Services.PrivateChatServices;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
public class WSHandler extends AbstractWebSocketHandler {
    private final PrivateChatServices privateChatServices;
    private final WebSocketSecurityServices webSocketSecurityServices;
    public WSHandler(PrivateChatServices privateChatServices, WebSocketSecurityServices webSocketSecurityServices) {
        this.privateChatServices = privateChatServices;
        this.webSocketSecurityServices = webSocketSecurityServices;
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
            privateChatServices.addMessageToQueue(message.getPayload());
            privateChatServices.sendingAllQueuedMessagesToUser(session);
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
