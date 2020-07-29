package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Controller
public class ChatController {

	private final SimpMessagingTemplate simpMessagingTemplate;
	private static final String GroupChat="BAN";

	//it is a temporary way to make sure to only send the message to the people in the group chat
	private static final ArrayList<String> GroupChatUsers=new ArrayList<String>(){
		{
			add("amir");
			add("sepehr");
			add("mmd");
		}
	};


	public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	/*-------------------- Group (Public) chat--------------------*/
	@MessageMapping("/sendMessage")
	@SendTo("/topic/public")
	public void sendMessage(@Payload ChatMessage chatMessage, MessageHeaders messageHeaders,
							StompHeaderAccessor stompHeaderAccessor ) {
		System.out.println(34+" "+messageHeaders);
		System.out.println(35+" "+stompHeaderAccessor);
		System.out.println(chatMessage.getContent());
		if(!GroupChatUsers.contains(chatMessage.getSender())){
			return;
		}
//		GroupChatUsers.forEach(user ->simpMessagingTemplate.convertAndSend("/topic/public/"+user,chatMessage));
		simpMessagingTemplate.convertAndSend("/topic/public/"+chatMessage.getGroupChats(),chatMessage);
//
	}

	@MessageMapping("/addUser")
	@SendTo("/topic/pubic")
	public ChatMessage addUser(@Payload ChatMessage chatMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		// Add user in web socket session
		System.out.println(headerAccessor);
		Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", chatMessage.getSender());
		return chatMessage;
	}


	/*--------------------Private chat--------------------*/

	@MessageMapping("/sendPrivateMessage/{username}/{otheruser}")
//	@SendTo("/queue/reply")
	public void sendPrivateMessage(@Payload ChatMessage chatMessage, @DestinationVariable String username,@DestinationVariable String otheruser
	,MessageHeaders messageHeaders,SimpMessageHeaderAccessor simpMessageHeaderAccessor,StompHeaderAccessor stompHeaderAccessor) {
		System.out.println(53+ " "+messageHeaders);
		System.out.println(54+" "+simpMessageHeaderAccessor);
		System.out.println(55+ " "+stompHeaderAccessor);

		simpMessagingTemplate.convertAndSend("/queue/"+username,chatMessage);
	 	simpMessagingTemplate.convertAndSend("/queue/"+otheruser,chatMessage);
//		simpMessagingTemplate.convertAndSendToUser(chatMessage.getSender().trim(),"queue/reply",chatMessage);

	}

	@MessageMapping("/addPrivateUser")
	@SendTo("/queue")
	public ChatMessage addPrivateUser(@Payload ChatMessage chatMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		// Add user in web socket session
		Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("private-username", chatMessage.getSender());
		return chatMessage;
	}





}
