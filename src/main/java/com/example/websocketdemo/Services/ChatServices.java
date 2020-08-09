package com.example.websocketdemo.Services;

import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Service
public class ChatServices {
    private final ChatRepo chatRepo;
    private final GroupChatRepo groupChatRepo;
    private final UserRepo userRepo;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;
    public ChatServices(ChatRepo chatRepo, GroupChatRepo groupChatRepo, UserRepo userRepo, SimpMessagingTemplate simpMessagingTemplate, AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate) {
        this.chatRepo = chatRepo;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void savingChats(ChatMessage chatMessage){
        chatRepo.save(chatMessage);
        System.out.println("this chat has been saved"+chatMessage.getSender());
    }

    public void deletingChat(ChatMessage chatMessage){
        chatRepo.deleteById(chatMessage.getId());
        System.out.println("chat has been deleted");

    }

    public GroupChat createGroupChat(String username,String groupName ){
        GroupChat groupChat=new GroupChat();
        List<ChatUser> members=new ArrayList<>();
        ChatUser chatUser=userRepo.findByUsername(username);
        List<ChatUser>admins=new ArrayList<>();
        members.add(chatUser);
        admins.add(chatUser);
        groupChat.setName(groupName);
        groupChat.setMembers(members);
        groupChat.setAdmins(admins);
        groupChatRepo.save(groupChat);
        System.out.println("Group chat has been created");
        return groupChat;
    }

    public void createUser(ChatUser user){
        userRepo.save(user);
    }

    public void deleteUser(String id){
        userRepo.deleteById(id);
    }

    public void addToGroupChat( String groupId,ChatUser user){
       Optional<GroupChat> groupChat1= groupChatRepo.findById(groupId);
       if(groupChat1.isPresent()){
           List<ChatUser> allUsers=groupChat1.get().getMembers();
           allUsers.add(user);
           groupChat1.ifPresent(groupChat2 -> {
               groupChat2.setMembers(allUsers);
               groupChatRepo.save(groupChat2);
           });
       }
    }

    public void deleteGroupChat(String groupId, ChatUser chatUser) {
        Optional<GroupChat> groupChat=groupChatRepo.findById(groupId);
        if(groupChat.isPresent()){
            List<ChatUser> admins=groupChat.get().getAdmins();
            if (admins.contains(chatUser)) {
                List<ChatUser> members=groupChat.get().getMembers();
                members.forEach(m->{
                    List<GroupChat> groupChats=m.getGroupChats();
                    groupChats.removeIf(groupChat1 -> groupChat1.getId().equals(groupId));
                });
                groupChatRepo.deleteById(groupId);
                System.out.println("groupChat deleted");
            }
        }

    }

    public void broadCastMessageToGroupChat(ChatMessage chatMessage,String groupName){
        Optional<GroupChat> groupChat = groupChatRepo.findByName(groupName);
        if(groupChat.isPresent()){
            if(chatMessage.getContentType().equals("text")||
                 chatMessage.getContentType().equals("link")){
             List<String> texts= groupChat.get().getTexts();
             texts.add(chatMessage.getTextContent());
             groupChat.get().setTexts(texts);
            } else {
             List <byte []> medias = groupChat.get().getMedias();
             medias.add(chatMessage.getMediaContent());
             groupChat.get().setMedias(medias);
            }
        }
        simpMessagingTemplate.convertAndSend("/topic/public/"+groupName,chatMessage);
    }

    public List<ChatUser> getAll(){
        return userRepo.findAll();
    }

    public List<GroupChat> getAllGroups(){
        return groupChatRepo.findAll();
    }

    public GroupChat getGroup(String groupId){
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        return groupChat.orElse(null);
    }

    public void createQueuesAndExchangeForPrivateChat(String sender, String receiver )  {
        // Alternate exchange is when a message doesnt have a proper key and there fore exchanges
        // like topic and direct cant publish it there fore we use an alternate
        // exchange which is fanout to be a publisher of those messages


        String exchangeName;
        exchangeName = compareNamesAlphabetically(sender, receiver);

        FanoutExchange exchange=new FanoutExchange(exchangeName,false,false,null);
        FanoutExchange deadLetter= new FanoutExchange("dead-letter-"+exchangeName,false,false,null);

        Map<String,Object> args= new HashMap<>();
        args.put("x-dead-letter-exchange","dead-letter-"+exchangeName);

        Queue ferestande =new Queue(sender,false,false,false,args);
        Queue girande = new Queue(receiver,false,false,false,args);

        // Queue for dead letter exchange
        Queue deadQueue = new Queue("deadQueue" + "-" + exchangeName, false, false, false, null);

        Binding first = BindingBuilder.bind(ferestande).to(exchange);
        Binding second = BindingBuilder.bind(girande).to(exchange);
        Binding third = BindingBuilder.bind(deadQueue).to(deadLetter);

        List<Object> all = Arrays.asList(exchange, deadLetter, ferestande, girande, deadQueue, first, second, third);
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.addQueueNames("sex");
        System.out.print("156");
        all.forEach(i -> {
            if (i.getClass().toString().endsWith("Queue")) {
                amqpAdmin.declareQueue((Queue) i);
            } else if (i.getClass().toString().endsWith("Exchange")) {
                amqpAdmin.declareExchange((Exchange) i);
            } else if (i.getClass().toString().endsWith("Binding")) {
                amqpAdmin.declareBinding((Binding) i);
            }
        });

//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setUri("ampq://guest:guest@localhost:61613/virtualHost");
//        Connection conn= factory.newConnection();
//        Channel channel= conn.createChannel();
        //this way i can  close a channel and do some things other than adminmq

    }

    public ChatMessage gettingMessagesFromQueues(String name){
        return (ChatMessage) rabbitTemplate.receiveAndConvert(name);
    }

    public void sendingMessageToExchange(ChatMessage chatMessage , String sender , String receiver){
        String exchangeName;
        exchangeName=compareNamesAlphabetically(sender,receiver);
        rabbitTemplate.convertAndSend(exchangeName,"",chatMessage);
    }

    public String compareNamesAlphabetically(String name, String name1){
        String fullName;
        if (name.compareTo(name1) > 0) {
            fullName = name + "_" + name1;
        } else {
            fullName = name1 + "_" + name;
        }
        return fullName;
    }
}
