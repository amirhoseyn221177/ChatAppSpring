package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

	private final SimpMessagingTemplate simpMessagingTemplate;

	public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	/*-------------------- Group (Public) chat--------------------*/
	@MessageMapping("/sendMessage")
	@SendTo("/topic/public")
	public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
		System.out.println(chatMessage.toString());
		return chatMessage;
	}

	@MessageMapping("/addUser")
	@SendTo("/topic/pubic")
	public ChatMessage addUser(@Payload ChatMessage chatMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		// Add user in web socket session
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		return chatMessage;
	}


	/*--------------------Private chat--------------------*/

	@MessageMapping("/sendPrivateMessage")
	//@SendTo("/queue/reply")
	public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
		simpMessagingTemplate.convertAndSendToUser(
				chatMessage.getReceiver().trim(), "/reply", chatMessage); 
		//return chatMessage;
	}

	@MessageMapping("/addPrivateUser")
	@SendTo("/queue/reply")
	public ChatMessage addPrivateUser(@Payload ChatMessage chatMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		// Add user in web socket session
		System.out.println(headerAccessor);
		headerAccessor.getSessionAttributes().put("private-username", chatMessage.getSender());
		return chatMessage;
	}
}
