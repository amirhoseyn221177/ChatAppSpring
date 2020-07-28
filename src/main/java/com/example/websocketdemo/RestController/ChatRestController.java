package com.example.websocketdemo.RestController;

import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

@RestController("/restchat")
public class ChatRestController {
    private final ChatServices chatServices;

    public ChatRestController(ChatServices chatServices) {
        this.chatServices = chatServices;
    }

    @GetMapping("/create")
    public ResponseEntity<?>createGroupChat(@RequestBody GroupChat groupChat){
        chatServices.createGroupChat(groupChat);
        return new ResponseEntity<>(groupChat, HttpStatus.CREATED);
    }

    @GetMapping("/addtogroup/{groupId}")
    public ResponseEntity<?> addToGroupChat(@RequestBody ChatUser chatUser,@PathVariable String groupId){
        chatServices.addToGroupChat(groupId,chatUser);
        return new ResponseEntity<>(chatUser,HttpStatus.OK);
    }
}
