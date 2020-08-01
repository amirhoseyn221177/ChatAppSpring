package com.example.websocketdemo.controller;

import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatMessage;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Objects;


@Controller
public class ChatController {

	private final SimpMessagingTemplate simpMessagingTemplate;
	private final ChatServices chatServices;
	private static final String GroupChat="BAN";

	//it is a temporary way to make sure to only send the message to the people in the group chat
	private static final ArrayList<String> GroupChatUsers=new ArrayList<String>(){
		{
			add("amir");
			add("sepehr");
			add("mmd");
		}
	};


	public ChatController(SimpMessagingTemplate simpMessagingTemplate, ChatServices chatServices) {
		this.simpMessagingTemplate = simpMessagingTemplate;
		this.chatServices = chatServices;
	}

	/*-------------------- Group (Public) chat--------------------*/
	@MessageMapping("/sendMessage")
	@SendTo("/topic/public")
	public void sendMessage(@Payload ChatMessage chatMessage, MessageHeaders messageHeaders,
							StompHeaderAccessor stompHeaderAccessor ) {

		if(!GroupChatUsers.contains(chatMessage.getSender())){
			return;
		}
		
		//getting binary data from base64 to decrease the size up to 33%
		//String raw=chatMessage.getTextContent().substring(24); because of the default prefix from filereader in js

		//byte [] binary = Base64.decodeBase64(raw);

		//converting it back to base to be able to send the file to through websocket
		//String data= Base64.encodeBase64String(binary);

//		simpMessagingTemplate.convertAndSend("/topic/public/"+chatMessage.getGroupChat(),chatMessage);
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

	@MessageMapping("/sendPrivateMessage/{username}/{otherUser}")
//	@SendTo("/queue/reply")
	public void sendPrivateMessage(@Payload ChatMessage chatMessage, @DestinationVariable String username,@DestinationVariable String otherUser,
								   StompHeaderAccessor stompHeaderAccessor) {
		String user= (String) Objects.requireNonNull(stompHeaderAccessor.getSessionAttributes()).get("private-username");
		String otherUser2= (String) stompHeaderAccessor.getSessionAttributes().get("private-receiver");
		System.out.println(stompHeaderAccessor);
		simpMessagingTemplate.convertAndSend("/queue/"+user,chatMessage);
	 	simpMessagingTemplate.convertAndSend("/queue/"+otherUser2,chatMessage);


	}

	@MessageMapping("/addPrivateUser")
	@SendTo("/queue")
	public ChatMessage addPrivateUser(@Payload ChatMessage chatMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		// Add user in web socket session
		System.out.println(chatMessage);
		Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("private-username", chatMessage.getSender());
		headerAccessor.getSessionAttributes().put("private-receiver",chatMessage.getReceiver());
		return chatMessage;
	}





}
