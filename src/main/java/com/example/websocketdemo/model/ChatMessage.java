package com.example.websocketdemo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;


@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    private String Id;
    private String textContent;
    private String  mediaContent ;
    @NotNull(message = "you should clarify the type of content")
    private String contentType;
    @NotNull(message = "you should declare a sender ")
    private String sender;
    private String groupChat;
    private String receiver;
	private Long dateTime=new Date().getTime();
	private String token;
	private String HistoryId;

}