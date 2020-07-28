package com.example.websocketdemo.Services;

import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatServices {
    private final ChatRepo chatRepo;
    private final GroupChatRepo groupChatRepo;
    private final UserRepo userRepo;

    public ChatServices(ChatRepo chatRepo, GroupChatRepo groupChatRepo, UserRepo userRepo) {
        this.chatRepo = chatRepo;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
    }

    public void savingChats(ChatMessage chatMessage){
        chatRepo.save(chatMessage);
        System.out.println("this chat has been saved"+chatMessage.getSender());
    }

    public void deletingChat(ChatMessage chatMessage){
        chatRepo.deleteById(chatMessage.getId());
        System.out.println("chat has been deleted");
    }

    public void createGroupChat(GroupChat groupChat ){
        groupChatRepo.save(groupChat);
        System.out.println("Group chat has been created");
    }

    public void createUser(ChatUser user){
        userRepo.save(user);
    }

    public void addToGroupChat( String groupId,ChatUser user){
       Optional<GroupChat> groupChat1= groupChatRepo.findById(groupId);
       if(groupChat1.isPresent()){
           List<ChatUser> allUsers=groupChat1.get().getMembers();
           allUsers.add(user);
           groupChat1.ifPresent(groupChat2 -> {
               groupChat2.setMembers(allUsers);
               groupChatRepo.save(groupChat2);
           });
       }
    }


}
