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

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
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
        BigInteger mod = new BigInteger("00c5963f6bfdbf8c878a1269264e9e905fa7f399acf544ed1f404a25e90ac54640e96a58d440ef9cfba83cc73bdf33ae0045e13a452b6092acc9fa2356e4cdda4eb5df2d9279b734dbde7cc3c9a1e0b1b8c1fa501ea4fe1eda9d45f35bfd2183cf2834d806dc597c83768e4e4e7cbf6d254c28342ac22f00292b4a969989b4c0c26d666dfd26a87f9383238dd9732701c64377f5888fa2e7246edeff4594a48a5687d41360da0d735b0cb73340b3442429efd29665df03252e237e5139e70fbad4c7cb4c94897c065384dce0a0a7f06f3c4dc7ecadc59116a0b6540bae5f2bd0f6e5a8f59e84a95ae7c6a5b28a8f2d9467013cdde3425b86601b2465e63e3c38b9"
                ,16);
        BigInteger pvtExp = new BigInteger(
                "7afbc20e1dab147568e37cbf73400eecfb450cea32dfec17ff9427749e579c1adb2e6e8beebf361f0de086c22573ad624e1c9a80633781143bc38e2b8fcef18afc79258c7e662268985b602228b80ae6cd1658b83a4a523a109a7197583f7c237d0b274b5debf52c7ea6a4e7058ec108ee163822452bac15f3f44138e015b9c432a659f664e0d4c1dd37945ace89f0680a81236212179e045c1601ae635b9cd8136f20a2deae091daf96220e8ed4d7d3ba88e1ad2142eba00dc18ff2e6b14a5085b2625543d218380653c1ccc3d510d51ee11fd07f9934d153cc4f604d81c6e89555f6f34725e009d5afc7b4c8a9ffbc607e825093dd96a505104d737996e061",
                16);
        BigInteger pubExp = new BigInteger("65537");
        RsaKey rsaKey= new RsaKey(mod,pvtExp,pubExp);
        PublicKey publicKey = rsaKey.gettingPublicKey();
        PrivateKey privateKey= rsaKey.gettingPrivateKey();
        ChatMessage message = new ChatMessage();
        message.setContentType("abudlah");
        message.setTextContent("salam lil baby ");
        message.setSender("amir2211");
        message.setReceiver("moh2211");

        List<String> encryptedMes= privateChatServices.encryption(message,publicKey);
        privateChatServices.decryption(encryptedMes,privateKey);
//        System.out.println(chatRepo.findAll());
//        System.out.println(userRepo.findAll());
////        System.out.println(groupChatRepo.findAll());
//        chatRepo.deleteAll();
//        userRepo.deleteAll();
//        groupChatRepo.deleteAll();

    }
}
