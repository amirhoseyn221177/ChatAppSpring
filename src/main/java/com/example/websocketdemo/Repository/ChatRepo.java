package com.example.websocketdemo.Repository;

import com.example.websocketdemo.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepo extends MongoRepository<ChatMessage,String> {
    void deleteById(String Id);
    void findBy
}
