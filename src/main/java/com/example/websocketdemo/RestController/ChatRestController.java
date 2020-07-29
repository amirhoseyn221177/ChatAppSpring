package com.example.websocketdemo.RestController;

import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restchat")
@CrossOrigin("*")
public class ChatRestController {
    private final ChatServices chatServices;

    public ChatRestController(ChatServices chatServices) {
        this.chatServices = chatServices;
    }

    @PostMapping("/create/{groupName}")
    public ResponseEntity<?>createGroupChat(@RequestBody ChatUser chatUser, @PathVariable String groupName){
        chatServices.createGroupChat(chatUser.getUsername(), groupName);
        return new ResponseEntity<>("has been created", HttpStatus.CREATED);
    }

    @PostMapping("/addtogroup/{groupId}")
    public ResponseEntity<?> addToGroupChat(@RequestBody ChatUser chatUser,@PathVariable String groupId){
        chatServices.addToGroupChat(groupId,chatUser);
        return new ResponseEntity<>(chatUser,HttpStatus.OK);
    }

    @DeleteMapping("/deletegroupchat/{groupId}")
    public ResponseEntity<?> deletingGroupChats(@RequestBody ChatUser chatUser,@PathVariable String groupId){
        chatServices.deleteGroupChat(groupId,chatUser);
        String jsonResponse=new Gson().toJson("groupChat has been deleted");
        System.out.println(jsonResponse);
        return new ResponseEntity<>(jsonResponse,HttpStatus.ACCEPTED);
    }

    @PostMapping("/createuser")
    public ResponseEntity<?> createUser(@RequestBody ChatUser chatUser){
        chatServices.createUser(chatUser);
        return  new ResponseEntity<>(chatUser,HttpStatus.CREATED);
    }
}
