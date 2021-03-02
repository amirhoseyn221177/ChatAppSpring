package com.example.websocketdemo.controller;

import com.example.websocketdemo.Services.GroupChatServices;
import com.example.websocketdemo.Services.PrivateChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/restchat")
@CrossOrigin("*")
public class ChatRestController {
    private final PrivateChatServices privateChatServices;
    private final GroupChatServices groupChatServices;

    public ChatRestController(PrivateChatServices privateChatServices, GroupChatServices groupChatServices) {
        this.privateChatServices = privateChatServices;
        this.groupChatServices = groupChatServices;
    }

    @PostMapping("/create/{groupName}")
    public ResponseEntity<?> createGroupChat(@RequestBody ChatUser chatUser, @PathVariable String groupName, BindingResult result) {
        GroupChat groupChat = groupChatServices.createGroupChat(chatUser.getUsername(), groupName);
        return new ResponseEntity<>(groupChat, HttpStatus.CREATED);
    }

    @PostMapping("/addtogroup/{groupId}/{username}")
    public ResponseEntity<?> addToGroupChat(@PathVariable String username, @PathVariable String groupId) {
        GroupChat groupChat = groupChatServices.addToGroupChat(groupId, username);
        return new ResponseEntity<>(groupChat, HttpStatus.OK);
    }

    @DeleteMapping("/deletegroupchat/{groupId}/{username}")
    public ResponseEntity<?> deletingGroupChats(@PathVariable String username, @PathVariable String groupId) {
        groupChatServices.deleteGroupChat(groupId, username);
        String jsonResponse = new Gson().toJson("groupChat has been deleted");
        System.out.println(jsonResponse);
        return new ResponseEntity<>(jsonResponse, HttpStatus.ACCEPTED);
    }

    @GetMapping("/getall")
    @PreAuthorize(" hasAuthority('ADMIN')")
    public ResponseEntity<?> gettingAllUsers() {
        return new ResponseEntity<>(privateChatServices.getAll(), HttpStatus.OK);
    }

    @GetMapping("/getallgroup")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> gettingAllGroup() {
        return new ResponseEntity<>(groupChatServices.getAllGroups(), HttpStatus.OK);
    }

    @GetMapping("/getgroup/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable String groupId) {
        return new ResponseEntity<>(groupChatServices.getGroup(groupId), HttpStatus.OK);
    }

    @DeleteMapping("/deleteuser/{userId}")
    public void deletingUser(@PathVariable String userId) {
        privateChatServices.deleteUser(userId);
    }


}
