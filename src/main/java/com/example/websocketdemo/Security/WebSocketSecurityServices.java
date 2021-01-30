package com.example.websocketdemo.Security;

import com.example.websocketdemo.model.ChatMessage;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class WebSocketSecurityServices {
    private final TokenValidator tokenValidator;

    public WebSocketSecurityServices(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    public boolean isTheMessageAuthorized(WebSocketSession session, TextMessage message){
        Gson gson= new Gson();
        if(!((boolean) session.getAttributes().get("authorization"))){
            ChatMessage chatMessage= gson.fromJson(message.getPayload(),ChatMessage.class);
            String token = chatMessage.getToken();
            boolean response= tokenValidator.validateToken(token);
            if (response) {
                session.getAttributes().replace("authorization", true);
            }
            return response;
        }
        return true;
    }
}
