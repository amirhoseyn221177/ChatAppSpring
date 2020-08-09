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
    private String contentType;
    private String sender;
    private String groupChat;
    private String receiver;
	private LocalDateTime dateTime=LocalDateTime.now();

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public byte[] getMediaContent() {
        return mediaContent;
    }

    public void setMediaContent(byte[] mediaContent) {
        this.mediaContent = mediaContent;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getGroupChat() {
        return groupChat;
    }

    public void setGroupChat(String groupChat) {
        this.groupChat = groupChat;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
