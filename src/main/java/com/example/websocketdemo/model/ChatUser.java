package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUser {
    @Id
    private String id;
    @NotBlank(message = "you should have a name ")
    private String name;
    @NotBlank(message = "each user has to have a username ")
    private String username;
    private List<String> groupChats = new ArrayList<>();
    private List<String> friends = new ArrayList<>();
    @NotBlank(message = "password should not be blank")
    @Size(min = 4,max = 10, message = "please use 4 to 10 characters")
    private String password;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getGroupChats() {
        return groupChats;
    }

    public void setGroupChats(List<String> groupChats) {
        this.groupChats = groupChats;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
