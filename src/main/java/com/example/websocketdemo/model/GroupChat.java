package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupChat {
    @Id
    private String id;
    private String name;
    private List<ChatUser> members;
    private List<ChatUser> admins;
    private List<String> texts = new ArrayList<>();
    private List<byte []> medias = new ArrayList<>();
    private List<ChatUser> activeUsers = new ArrayList<>();

}
