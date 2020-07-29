package com.example.websocketdemo.Repository;

import com.example.websocketdemo.model.ChatUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends MongoRepository<ChatUser,String> {
    ChatUser findByUsername(String username);
}
