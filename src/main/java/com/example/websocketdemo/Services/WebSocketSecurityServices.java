package com.example.websocketdemo.Services;

import com.example.websocketdemo.Exceptions.BadTokenException;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.Security.TokenValidator;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatUser;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebSocketSecurityServices {
    private final TokenValidator tokenValidator;
    private final UserRepo userRepo;
    private final CustomUserServices customUserServices;
    public WebSocketSecurityServices(TokenValidator tokenValidator, UserRepo userRepo, CustomUserServices customUserServices) {
        this.tokenValidator = tokenValidator;
        this.userRepo = userRepo;
        this.customUserServices = customUserServices;
    }

    public boolean isTheMessageAuthorized(WebSocketSession session, TextMessage message) {
        try {
            Gson gson = new Gson();
            ChatMessage chatMessage = gson.fromJson(message.getPayload(), ChatMessage.class);
            String token = chatMessage.getToken();
            String jwt = tokenValidator.getJwtFromRequest(token);
            boolean response = (boolean) doFilterForSockets(jwt, chatMessage.getSender()).get("authenticated");
            System.out.println("response" + " " + response);
            if (response) {
                session.getAttributes().replace("authorization", true);
            }else{
                session.getAttributes().replace("authorization", false);
            }
            return response;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }

    }




    public Map<String,Object> doFilterForSockets(String token, String username){
        Map<String,Object> answer = new HashMap<>();
        try{
            boolean resp=tokenValidator.validateToken(token);
            if(resp){
                String userId=tokenValidator.GetIdFromToken(token);
                ChatUser retrievedUser=customUserServices.loadByID(userId);
                if(username==null || !username.equals(retrievedUser.getUsername())){
                    throw new BadTokenException("the Token is mis-formed");
                }
                UsernamePasswordAuthenticationToken authenticationToken= new UsernamePasswordAuthenticationToken(
                        retrievedUser.getUsername(),null,getTheRole(token)
                );
                answer.put("authenticated",true);
                answer.put("authenticationToken",authenticationToken);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            answer.put("authenticated",false);
            return  answer;
        }

        return answer;
    }

    public List<SimpleGrantedAuthority> getTheRole(String token){
        Claims claims=tokenValidator.getClaimsFromToken(token);
        List<String> roles=claims.get("roles",List.class);
        List<SimpleGrantedAuthority>authorities=roles.stream().map(role->
                new SimpleGrantedAuthority(role)).collect(Collectors.toList());
        return authorities;
    }
}
