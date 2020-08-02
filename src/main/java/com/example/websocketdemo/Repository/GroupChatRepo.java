package com.example.websocketdemo.Repository;

import com.example.websocketdemo.model.GroupChat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupChatRepo extends MongoRepository<GroupChat,String> {
    void deleteById(String id);
    Optional<GroupChat> findByName(String name);
}
