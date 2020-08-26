package com.example.websocketdemo.controller;

import com.example.websocketdemo.Exceptions.MapValidationError;
import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.LoginRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/user")
@CrossOrigin("*")

public class UserRestController {

    private final MapValidationError mapValidationError;
    private final ChatServices chatServices;


    public UserRestController(MapValidationError mapValidationError, ChatServices chatServices) {
        this.mapValidationError = mapValidationError;
        this.chatServices = chatServices;

    }

    @PostMapping("/register")
    public ResponseEntity<?> registering(@RequestBody ChatUser chatUser, BindingResult result) {
        System.out.println(chatUser);
        ResponseEntity<?> error = mapValidationError.MapValidationService(result);
        if (error != null) return error;
        ChatUser chatUser1 = chatServices.createUser(chatUser);
        return new ResponseEntity<>(chatUser1, HttpStatus.ACCEPTED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, BindingResult result,
                                   @RequestHeader Map<String, Object> allHeaders) {
        System.out.println(allHeaders);
        System.out.println(loginRequest);
        ResponseEntity<?> error = mapValidationError.MapValidationService(result);
        if (error != null) return error;
        String jwt = chatServices.SendToken(loginRequest.getUsername(), loginRequest.getPassword());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", jwt);
        Map<String, Object> body = new HashMap<>();
        body.put("token", jwt);
        UsernamePasswordAuthenticationToken authentication= new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),loginRequest.getPassword(), Collections.emptyList()
        ) ;

      SecurityContextHolder.getContext().setAuthentication(authentication);

        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }
}
