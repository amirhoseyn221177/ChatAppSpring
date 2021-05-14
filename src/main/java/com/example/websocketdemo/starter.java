package com.example.websocketdemo;

import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.PrivateChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.Services.PrivateChatServices;
import com.example.websocketdemo.config.AWSConfig;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.RsaKey;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import java.math.BigInteger;
import java.security.*;
import java.util.Date;
import java.util.List;

@Component
public class starter implements CommandLineRunner {
    private final ChatRepo chatRepo;
    private final UserRepo userRepo;
    private final GroupChatRepo groupChatRepo;
    private final AWSConfig awsConfig;
    private final PrivateChatServices privateChatServices;
    private final Environment env;
    private  final PrivateChatRepo privateChatRepo;
    public starter(ChatRepo chatRepo, UserRepo userRepo, GroupChatRepo groupChatRepo, AWSConfig awsConfig, PrivateChatServices privateChatServices, Environment env, PrivateChatRepo privateChatRepo) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
        this.groupChatRepo = groupChatRepo;
        this.awsConfig = awsConfig;
        this.privateChatServices = privateChatServices;
        this.env = env;
        this.privateChatRepo = privateChatRepo;
    }

    @Override
    public void run(String... args) throws Exception {

        RsaKey rsaKey= new RsaKey();
        ChatMessage message = new ChatMessage();
        message.setContentType("abudlah");
        message.setTextContent("salam lil baby ");
        message.setSender("amir2211");
        message.setReceiver("moh2211");





        List<String> encryptedMes= privateChatServices.encryption(message,rsaKey.getPublicKey());
        privateChatServices.decryption(encryptedMes,rsaKey.getPrivateKey());


//        System.out.println(chatRepo.findAll());
//        System.out.println(userRepo.findAll());
////        System.out.println(groupChatRepo.findAll());
//        chatRepo.deleteAll();
//        userRepo.deleteAll();
//        groupChatRepo.deleteAll();

    }
}
