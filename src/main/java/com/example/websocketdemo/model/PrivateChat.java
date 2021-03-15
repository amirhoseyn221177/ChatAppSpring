package com.example.websocketdemo.model;

import com.example.websocketdemo.model.ChatUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChat {
    @Id
    private String Id;
    private List<String> users=new ArrayList<>();
    private List<ChatMessage> messages = new ArrayList<>();
}
