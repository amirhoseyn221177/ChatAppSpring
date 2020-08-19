package com.example.websocketdemo.Repository;

import com.example.websocketdemo.model.ChatUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<ChatUser,String> {
    Optional<ChatUser> findByUsername(String username);
}
