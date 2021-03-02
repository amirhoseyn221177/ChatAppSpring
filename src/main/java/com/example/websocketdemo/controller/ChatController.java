package com.example.websocketdemo.controller;

import com.example.websocketdemo.Services.GroupChatServices;
import com.example.websocketdemo.Services.PrivateChatServices;
import com.example.websocketdemo.model.ChatMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.Objects;


//@Controller
public class ChatController {

	private static final String GroupChat = "BAN";
	private final PrivateChatServices privateChatServices;
	private final RabbitTemplate rabbitTemplate;
	private final GroupChatServices groupChatServices;


	public ChatController(PrivateChatServices privateChatServices, RabbitTemplate rabbitTemplate, GroupChatServices groupChatServices) {

		this.privateChatServices = privateChatServices;
		this.rabbitTemplate = rabbitTemplate;

		this.groupChatServices = groupChatServices;
	}

	/*-------------------- Group (Public) chat--------------------*/
	@MessageMapping("/sendMessage")
	public void sendMessage(@Payload ChatMessage chatMessage, MessageHeaders messageHeaders,
							StompHeaderAccessor stompHeaderAccessor) {


//		GroupChat intended_groupChat = chatServices.getGroupByName(chatMessage.getGroupChat());
//		System.out.println(stompHeaderAccessor);
		//getting binary data from base64 to decrease the size up to 33%
		//String raw=chatMessage.getTextContent().substring(24); because of the default prefix from filereader in js

		//byte [] binary = Base64.decodeBase64(raw);

		//converting it back to base to be able to send the file to through websocket
		//String data= Base64.encodeBase64String(binary);
		groupChatServices.broadCastMessageToGroupChat(chatMessage, chatMessage.getGroupChat());

	}

	@MessageMapping("/addUser")
	public void addUser(@Payload ChatMessage chatMessage,
							   SimpMessageHeaderAccessor headerAccessor, StompHeaderAccessor stompHeaderAccessor) {
		// Add user in web socket session
		groupChatServices.createQueuesAndExchangeForGroupChat(chatMessage.getGroupChat(),chatMessage.getSender());
		Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", chatMessage.getSender());
		headerAccessor.getSessionAttributes().put("groupName", chatMessage.getGroupChat());

	}


	/*--------------------Private chat--------------------*/

	@MessageMapping("/sendPrivateMessage/{username}")
	public void sendPrivateMessage(@Payload ChatMessage chatMessage,
								   SimpMessageHeaderAccessor simpMessageHeaderAccessor)  {
		privateChatServices.sendPrivateMessage(chatMessage,simpMessageHeaderAccessor);

	}



//	@MessageMapping("/addPrivateUser/{name}")
//	public ChatMessage addPrivateUser(@Payload ChatMessage chatMessage,
//									  SimpMessageHeaderAccessor headerAccessor) {
//		// Add user in web socket session
//		Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("private-username", chatMessage.getSender());
//		headerAccessor.getSessionAttributes().put("private-receiver", chatMessage.getReceiver());
//		headerAccessor.getSessionAttributes().put("exchangeName", headerAccessor.getNativeHeader("exchangeName"));
////		if(!chatServices.receiverExist(chatMessage.getSender(),chatMessage.getReceiver())){
////			return null;
////		}
//		chatServices.createQueuesAndExchangeForPrivateChat(chatMessage.getSender(), chatMessage.getReceiver());
//		return chatMessage;
//	}


}
