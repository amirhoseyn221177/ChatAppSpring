package com.example.websocketdemo;

import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.config.AWSConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class starter implements CommandLineRunner {
    private final ChatRepo chatRepo;
    private final UserRepo userRepo;
    private final GroupChatRepo groupChatRepo;
    private final AWSConfig awsConfig;

    public starter(ChatRepo chatRepo, UserRepo userRepo, GroupChatRepo groupChatRepo, AWSConfig awsConfig) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
        this.groupChatRepo = groupChatRepo;
        this.awsConfig = awsConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(chatRepo.findAll());
        System.out.println(userRepo.findAll());
        System.out.println(groupChatRepo.findAll());
//        awsConfig.preSignedURl();
//        chatRepo.deleteAll();
//        userRepo.deleteAll();
//        groupChatRepo.deleteAll();
    }
}
