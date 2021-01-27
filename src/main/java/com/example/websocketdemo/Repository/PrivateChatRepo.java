package com.example.websocketdemo.Repository;

import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.PrivateChat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivateChatRepo extends MongoRepository<PrivateChat,String> {
    Optional<PrivateChat> findByUsers(List<String> users);
}
