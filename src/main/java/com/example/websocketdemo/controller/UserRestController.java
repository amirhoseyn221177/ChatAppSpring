package com.example.websocketdemo.controller;

import com.example.websocketdemo.Exceptions.MapValidationError;
import com.example.websocketdemo.Services.PrivateChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.LoginRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin("*")

public class UserRestController {

    private final MapValidationError mapValidationError;
    private final PrivateChatServices privateChatServices;


    public UserRestController(MapValidationError mapValidationError, PrivateChatServices privateChatServices) {
        this.mapValidationError = mapValidationError;
        this.privateChatServices = privateChatServices;

    }

    @PostMapping("/register")
    public ResponseEntity<?> registering(@RequestBody ChatUser chatUser, BindingResult result) {
        try {
            ResponseEntity<?> error = mapValidationError.MapValidationService(result);
            if (error != null) return error;
            ChatUser chatUser1 = privateChatServices.createUser(chatUser);
            return new ResponseEntity<>(chatUser1, HttpStatus.ACCEPTED);
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
            return new ResponseEntity<>("this user name is taken", HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, BindingResult result,
                                   @RequestHeader Map<String, Object> allHeaders) {

        ResponseEntity<?> error = mapValidationError.MapValidationService(result);
        if (error != null) return error;
        String jwt = privateChatServices.SendToken(loginRequest.getUsername(), loginRequest.getPassword());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", jwt);
        Map<String, Object> body = new HashMap<>();
        body.put("token", jwt);
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/download")
    public void downloadingFile(){
        privateChatServices.downloadFromS3();
    }

}
