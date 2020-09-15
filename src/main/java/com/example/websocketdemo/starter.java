package com.example.websocketdemo;

import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.config.AWSConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class starter implements CommandLineRunner {
    private final ChatRepo chatRepo;
    private final UserRepo userRepo;
    private final GroupChatRepo groupChatRepo;
    private final AWSConfig awsConfig;
    private final ChatServices chatServices;

    public starter(ChatRepo chatRepo, UserRepo userRepo, GroupChatRepo groupChatRepo, AWSConfig awsConfig, ChatServices chatServices) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
        this.groupChatRepo = groupChatRepo;
        this.awsConfig = awsConfig;
        this.chatServices = chatServices;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(chatRepo.findAll());
        System.out.println(userRepo.findAll());
        System.out.println(groupChatRepo.findAll());
//        System.out.println(  Files.size(Path.of("/home/amir/Downloads/stream.mp4")));
//        awsConfig.preSignedURl();
//        chatRepo.deleteAll();
//        userRepo.deleteAll();
//        groupChatRepo.deleteAll();
    }
}
