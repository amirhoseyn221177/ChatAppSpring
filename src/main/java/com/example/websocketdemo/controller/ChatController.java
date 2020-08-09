package com.example.websocketdemo.controller;

import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.*;


@Controller
public class ChatController {

	private final SimpMessagingTemplate simpMessagingTemplate;
	private final ChatServices chatServices;
	private static final String GroupChat="BAN";
	private  final  RabbitTemplate rabbitTemplate;
	//it is a temporary way to make sure to only send the message to the people in the group chat
	private static final ArrayList<String> GroupChatUsers=new ArrayList<String>(){
		{
			add("amir");
			add("sepehr");
			add("mmd");
		}
	};


	public ChatController(SimpMessagingTemplate simpMessagingTemplate, ChatServices chatServices, RabbitTemplate rabbitTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
		this.chatServices = chatServices;
		this.rabbitTemplate = rabbitTemplate;
	}

	/*-------------------- Group (Public) chat--------------------*/
	@MessageMapping("/sendMessage")
	@SendTo("/topic/public")
	public void sendMessage(@Payload ChatMessage chatMessage, MessageHeaders messageHeaders,
							StompHeaderAccessor stompHeaderAccessor ) {

		if(!GroupChatUsers.contains(chatMessage.getSender())){
			return;
		}
//		GroupChat intended_groupChat = chatServices.getGroupByName(chatMessage.getGroupChat());
//		System.out.println(stompHeaderAccessor);
		//getting binary data from base64 to decrease the size up to 33%
		//String raw=chatMessage.getTextContent().substring(24); because of the default prefix from filereader in js

		//byte [] binary = Base64.decodeBase64(raw);

		//converting it back to base to be able to send the file to through websocket
		//String data= Base64.encodeBase64String(binary);

		chatServices.broadCastMessageToGroupChat(chatMessage,chatMessage.getGroupChat());

	}

	@MessageMapping("/addUser")
	@SendTo("/topic/pubic")
	public ChatMessage addUser(@Payload ChatMessage chatMessage,
			SimpMessageHeaderAccessor headerAccessor,StompHeaderAccessor stompHeaderAccessor) {
		System.out.println(stompHeaderAccessor);
		// Add user in web socket session
		Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", chatMessage.getSender());
		headerAccessor.getSessionAttributes().put("groupname",chatMessage.getGroupChat());
		return chatMessage;
	}


	/*--------------------Private chat--------------------*/

	@MessageMapping("/sendPrivateMessage/{username}")
//	@SendTo("/queue")
	public void sendPrivateMessage(@Payload ChatMessage chatMessage, @DestinationVariable String username,
								   StompHeaderAccessor stompHeaderAccessor) {

//		chatServices.createQueuesAndExchangeForPrivateChat(chatMessage.getSender(),chatMessage.getReceiver());
		System.out.println(chatMessage.getSender());
		simpMessagingTemplate.convertAndSend("/queue/device."+chatMessage.getSender(),chatMessage);



	}

	@MessageMapping("/addPrivateUser/{name}")
	@SendTo("/queue/device.{name}")
	public ChatMessage addPrivateUser(@Payload ChatMessage chatMessage,@DestinationVariable String name,
									  SimpMessageHeaderAccessor headerAccessor) {
		// Add user in web socket session

		Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("private-username", chatMessage.getSender());
		headerAccessor.getSessionAttributes().put("private-receiver",chatMessage.getReceiver());
//		chatServices.createQueuesAndExchangeForPrivateChat(chatMessage.getSender() ,chatMessage.getReceiver());
		return chatMessage;
	}





}
