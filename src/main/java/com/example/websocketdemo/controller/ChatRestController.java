package com.example.websocketdemo.controller;

import com.example.websocketdemo.Exceptions.MapValidationError;
import com.example.websocketdemo.Security.TokenValidator;
import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import com.example.websocketdemo.model.LoginRequest;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/restchat")
@CrossOrigin("*")
public class ChatRestController {
    private final ChatServices chatServices;
    private final MapValidationError mapValidationError;
    private final AuthenticationManager authenticationManager;
    private final TokenValidator tokenValidator;
    public ChatRestController(ChatServices chatServices, MapValidationError mapValidationError, AuthenticationManager authenticationManager, TokenValidator tokenValidator) {
        this.chatServices = chatServices;
        this.mapValidationError = mapValidationError;
        this.authenticationManager = authenticationManager;
        this.tokenValidator = tokenValidator;
    }

    @PostMapping("/create/{groupName}")
    public ResponseEntity<?> createGroupChat(@RequestBody ChatUser chatUser, @PathVariable String groupName, BindingResult result) {
        GroupChat groupChat = chatServices.createGroupChat(chatUser.getUsername(), groupName);
        return new ResponseEntity<>(groupChat, HttpStatus.CREATED);
    }

    @PostMapping("/addtogroup/{groupId}/{username}")
    public ResponseEntity<?> addToGroupChat(@PathVariable String username, @PathVariable String groupId) {
        GroupChat groupChat = chatServices.addToGroupChat(groupId, username);
        return new ResponseEntity<>(groupChat, HttpStatus.OK);
    }

    @DeleteMapping("/deletegroupchat/{groupId}/{username}")
    public ResponseEntity<?> deletingGroupChats(@PathVariable String username, @PathVariable String groupId) {
        chatServices.deleteGroupChat(groupId, username);
        String jsonResponse = new Gson().toJson("groupChat has been deleted");
        System.out.println(jsonResponse);
        return new ResponseEntity<>(jsonResponse, HttpStatus.ACCEPTED);
    }
    @GetMapping("/getall/{userId}")
    @PreAuthorize("principal== #userId" )
    public ResponseEntity<?> gettingAllUsers(@PathVariable String userId) {
        return new ResponseEntity<>(chatServices.getAll(), HttpStatus.OK);
    }

    @GetMapping("/getallgroup")
    public ResponseEntity<?> gettingAllGroup() {
        return new ResponseEntity<>(chatServices.getAllGroups(), HttpStatus.OK);
    }

    @GetMapping("/getgroup/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable String groupId) {
        return new ResponseEntity<>(chatServices.getGroup(groupId), HttpStatus.OK);
    }

    @DeleteMapping("/deleteuser/{userId}")
    public void deletingUser(@PathVariable String userId) {
        chatServices.deleteUser(userId);
    }


}
