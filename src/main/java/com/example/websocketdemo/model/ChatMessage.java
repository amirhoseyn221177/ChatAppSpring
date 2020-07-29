package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;


@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    private String Id;
    private MessageType type;
    private String content;
    private String sender;
    private String receiver;
    private String groupChats;
	private LocalDateTime dateTime=LocalDateTime.now();
    
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING
    }

//    public String getId() {
//        return Id;
//    }
//
//    public void setId(String id) {
//        Id = id;
//    }
//
//    public MessageType getType() {
//        return type;
//    }
//
//    public void setType(MessageType type) {
//        this.type = type;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    public String getSender() {
//        return sender;
//    }
//
//    public void setSender(String sender) {
//        this.sender = sender;
//    }
//
//    public String getReceiver() {
//		return receiver;
//	}
//
//	public void setReceiver(String receiver) {
//		this.receiver = receiver;
//	}
//
//    public LocalDateTime getDateTime() {
//		return dateTime;
//	}
//
//	public void setDateTime(LocalDateTime dateTime) {
//		this.dateTime = dateTime;
//	}
//
//    public ArrayList<String> getGroupChats() {
//        return GroupChats;
//    }
//
//    public void setGroupChats(ArrayList<String> groupChats) {
//        GroupChats = groupChats;
//    }
//
//    @Override
//    public String toString() {
//        return "ChatMessage{" +
//                "type=" + type +
//                ", content='" + content + '\'' +
//                ", sender='" + sender + '\'' +
//                ", receiver='" + receiver + '\'' +
//                ", dateTime=" + dateTime +
//                '}';
//    }
}
