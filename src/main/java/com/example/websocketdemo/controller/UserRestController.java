package com.example.websocketdemo.controller;

import com.example.websocketdemo.Exceptions.MapValidationError;
import com.example.websocketdemo.Security.TokenValidator;
import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.LoginRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/user")
@CrossOrigin("*")

public class UserRestController {

    private final MapValidationError mapValidationError;
    private final ChatServices chatServices;
    private final AuthenticationManager authenticationManager;
    private final TokenValidator tokenValidator;

    public UserRestController(MapValidationError mapValidationError, ChatServices chatServices, AuthenticationManager authenticationManager, TokenValidator tokenValidator) {
        this.mapValidationError = mapValidationError;
        this.chatServices = chatServices;
        this.authenticationManager = authenticationManager;
        this.tokenValidator = tokenValidator;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registering(@RequestBody  ChatUser chatUser, BindingResult result) {
        System.out.println(chatUser);
        ResponseEntity<?> error = mapValidationError.MapValidationService(result);
        if (error != null) return error;
       ChatUser chatUser1= chatServices.createUser(chatUser);
        return new ResponseEntity<>(chatUser1, HttpStatus.ACCEPTED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, BindingResult result,
                                   @RequestHeader Map<String,Object> allHeaders){
        System.out.println(allHeaders);
        System.out.println(loginRequest);
        ResponseEntity<?> error = mapValidationError.MapValidationService(result);
        if(error!=null)return error;
        String jwt =chatServices.SendToken(loginRequest.getUsername(), loginRequest.getPassword());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization",jwt);
        Map<String ,Object> body= new HashMap<>();
        body.put("Token",jwt);
        return new ResponseEntity<>(body,httpHeaders,HttpStatus.OK);
    }
}
