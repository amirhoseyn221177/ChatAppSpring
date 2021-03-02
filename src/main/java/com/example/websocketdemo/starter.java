package com.example.websocketdemo;

import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.Services.PrivateChatServices;
import com.example.websocketdemo.config.AWSConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class starter implements CommandLineRunner {
    private final ChatRepo chatRepo;
    private final UserRepo userRepo;
    private final GroupChatRepo groupChatRepo;
    private final AWSConfig awsConfig;
    private final PrivateChatServices privateChatServices;

    public starter(ChatRepo chatRepo, UserRepo userRepo, GroupChatRepo groupChatRepo, AWSConfig awsConfig, PrivateChatServices privateChatServices) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
        this.groupChatRepo = groupChatRepo;
        this.awsConfig = awsConfig;
        this.privateChatServices = privateChatServices;
    }

    @Override
    public void run(String... args) throws Exception {
//        System.out.println(chatRepo.findAll());
//        System.out.println(userRepo.findAll());
////        System.out.println(groupChatRepo.findAll());
//        chatRepo.deleteAll();
//        userRepo.deleteAll();
//        groupChatRepo.deleteAll();
    }
}
