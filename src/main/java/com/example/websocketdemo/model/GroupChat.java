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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChatUser> getMembers() {
        return members;
    }

    public void setMembers(List<ChatUser> members) {
        this.members = members;
    }

    public List<ChatUser> getAdmins() {
        return admins;
    }

    public void setAdmins(List<ChatUser> admins) {
        this.admins = admins;
    }

    public List<String> getTexts() {
        return texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }

    public List<byte[]> getMedias() {
        return medias;
    }

    public void setMedias(List<byte[]> medias) {
        this.medias = medias;
    }

    public List<ChatUser> getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(List<ChatUser> activeUsers) {
        this.activeUsers = activeUsers;
    }
}
