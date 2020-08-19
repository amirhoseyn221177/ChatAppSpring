package com.example.websocketdemo.RestController;

import com.example.websocketdemo.Exceptions.MapValidationError;
import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/restchat")
@CrossOrigin("*")
public class ChatRestController {
    private final ChatServices chatServices;
    private final MapValidationError mapValidationError;

    public ChatRestController(ChatServices chatServices, MapValidationError mapValidationError) {
        this.chatServices = chatServices;
        this.mapValidationError = mapValidationError;
    }

    @PostMapping("/create/{groupName}")
    public ResponseEntity<?>createGroupChat(@RequestBody  ChatUser chatUser, @PathVariable String groupName , BindingResult result){
       GroupChat groupChat= chatServices.createGroupChat(chatUser.getUsername(), groupName);
        return new ResponseEntity<>(groupChat, HttpStatus.CREATED);
    }

    @PostMapping("/addtogroup/{groupId}/{username}")
    public ResponseEntity<?> addToGroupChat(@PathVariable String username,@PathVariable String groupId){
        GroupChat groupChat=   chatServices.addToGroupChat(groupId,username);
        return new ResponseEntity<>(groupChat,HttpStatus.OK);
    }

    @DeleteMapping("/deletegroupchat/{groupId}")
    public ResponseEntity<?> deletingGroupChats(@RequestBody ChatUser chatUser,@PathVariable String groupId){
        chatServices.deleteGroupChat(groupId,chatUser);
        String jsonResponse=new Gson().toJson("groupChat has been deleted");
        System.out.println(jsonResponse);
        return new ResponseEntity<>(jsonResponse,HttpStatus.ACCEPTED);
    }

    @PostMapping("/createuser")
    public ResponseEntity<?> createUser(@RequestBody  @Valid  ChatUser chatUser,BindingResult result){

        ResponseEntity<?> error =mapValidationError.MapValidationService(result);
        if(error!=null){
            return error;
        }
        chatServices.createUser(chatUser);
        return  new ResponseEntity<>(chatUser,HttpStatus.CREATED);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> gettingAllUsers(){
        return new ResponseEntity<>(chatServices.getAll(),HttpStatus.OK);
    }

    @GetMapping("/getallgroup")
    public ResponseEntity<?> gettingAllGroup(){
        return new ResponseEntity<>(chatServices.getAllGroups(),HttpStatus.OK);
    }

    @GetMapping("/getgroup/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable String groupId){
       return new ResponseEntity<>(chatServices.getGroup(groupId),HttpStatus.OK);
    }

    @DeleteMapping("/deleteuser/{userId}")
    public void deletingUser(@PathVariable String userId){
        chatServices.deleteUser(userId);
    }

}
