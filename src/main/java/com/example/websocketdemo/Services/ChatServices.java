package com.example.websocketdemo.Services;

import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public void createGroupChat(String username,String groupName ){
        GroupChat groupChat=new GroupChat();
        List<ChatUser> members=new ArrayList<>();
        ChatUser chatUser=userRepo.findByUsername(username);
        List<ChatUser>admins=new ArrayList<>();
        members.add(chatUser);
        admins.add(chatUser);
        groupChat.setName(groupName);
        groupChat.setMembers(members);
        groupChat.setAdmins(admins);
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

    public void deleteGroupChat(String groupId, ChatUser chatUser) {
        Optional<GroupChat> groupChat=groupChatRepo.findById(groupId);
        if(groupChat.isPresent()){
            List<ChatUser> admins=groupChat.get().getAdmins();
            if (admins.contains(chatUser)) {
                List<ChatUser> members=groupChat.get().getMembers();
                members.forEach(m->{
                    List<GroupChat> groupChats=m.getGroupChats();
                    groupChats.removeIf(groupChat1 -> groupChat1.getId().equals(groupId));
                });
                groupChatRepo.deleteById(groupId);
                System.out.println("groupChat deleted");
            }
        }

    }


}
