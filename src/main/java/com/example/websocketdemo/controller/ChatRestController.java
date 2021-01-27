package com.example.websocketdemo.controller;

import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/restchat")
@CrossOrigin("*")
public class ChatRestController {
    private final ChatServices chatServices;

    public ChatRestController(ChatServices chatServices) {
        this.chatServices = chatServices;
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

    @GetMapping("/getall")
    @PreAuthorize(" hasAuthority('ADMIN')")
    public ResponseEntity<?> gettingAllUsers() {
        return new ResponseEntity<>(chatServices.getAll(), HttpStatus.OK);
    }

    @GetMapping("/getallgroup")
    @PreAuthorize("hasAuthority('ADMIN')")
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

    @GetMapping("/presignedurl")
    public ResponseEntity<?> sendingPreSignedURL(){
        URL url =chatServices.gettingPreSigned();
        Map<String,Object> urls=new HashMap<>();
        urls.put("link",url);
        return new ResponseEntity<>(urls,HttpStatus.OK);
    }

}
