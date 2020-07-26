package com.example.websocketdemo.ChatServices;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class chatService {
    private static final ArrayList<String> GroupChatUsers=new ArrayList<>();

    public chatService() {
        GroupChatUsers.add("amir");
        GroupChatUsers.add("sepehr");
        GroupChatUsers.add("mmd");
    }
}
