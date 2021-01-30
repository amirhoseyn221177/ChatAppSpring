package com.example.websocketdemo.config;

import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.RabbitTools;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

@Component
public class WSHandler extends AbstractWebSocketHandler {
    private final ChatServices chatServices;

    public WSHandler(ChatServices chatServices) {
        this.chatServices = chatServices;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("in connection established");
        System.out.println(session);
        chatServices.savingUserSessions(session);
        chatServices.sendingAllQueuedMessagesToUser(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println(session.getAttributes().get("authorization"));

        if(session.getAttributes().get("authorization")!=null ) {
            if(!chatServices.checkAuthorization(session))
        {

            System.out.println(40);
            System.out.println(session);
            session.sendMessage(new TextMessage("sorry you are not authorized"));
            session.getAttributes().remove("authorization");
            System.out.println(session.getAttributes());
        }
        }
        else if(message.getPayload().length()>0){
            System.out.println(44);
            chatServices.createQueuesAndExchangeForPrivateChat(message.getPayload());
            chatServices.addMessageToQueue(message.getPayload());
        }else {
            session.sendMessage(new TextMessage("its not possible to start chatting with your friends+"));
        }

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        chatServices.deletingUserFromSessions(session);
    }
}
