package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;



@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    private String Id;
    private String textContent;
    private byte [] mediaContent;
    private ContentType contentType;
    private String sender;
    private String groupChat;
    private String receiver;
	private LocalDateTime dateTime=LocalDateTime.now();
    
    public enum ContentType {
        TEXT,
        VIDEO,
        IMAGE,
        LINK
    }

}
