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

import java.util.Objects;

@Controller
public class ChatController {

	private final SimpMessagingTemplate simpMessagingTemplate;

	public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	/*-------------------- Group (Public) chat--------------------*/
	@MessageMapping("/sendMessage")
	@SendTo("/topic/public")
	public ChatMessage sendMessage(@Payload ChatMessage chatMessage, MessageHeaders messageHeaders, StompHeaderAccessor stompHeaderAccessor) {

		System.out.println(messageHeaders.toString()+"  "+ 29);
		System.out.println(stompHeaderAccessor+" "+31);
		return chatMessage;
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
	public void sendPrivateMessage(@Payload ChatMessage chatMessage, @DestinationVariable String username,@DestinationVariable String otheruser) {
		System.out.println(52);
		System.out.println(username);
	 	simpMessagingTemplate.convertAndSend("/queue/"+username,chatMessage);
	 	simpMessagingTemplate.convertAndSend("/queue/"+otheruser,chatMessage);
//		simpMessagingTemplate.convertAndSendToUser(chatMessage.getSender().trim(),"queue/reply",chatMessage);

	}

	@MessageMapping("/addPrivateUser")
	@SendTo("/queue")
	public ChatMessage addPrivateUser(@Payload ChatMessage chatMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		// Add user in web socket session
		headerAccessor.getSessionAttributes().put("private-username", chatMessage.getSender());
		simpMessagingTemplate.convertAndSendToUser(chatMessage.getReceiver(),"/queue","user has been connected");
		return chatMessage;
	}
}
