package com.example.websocketdemo.Services;

import com.amazonaws.HttpMethod;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.xray.model.Http;
import com.amazonaws.util.IOUtils;
import com.example.websocketdemo.Exceptions.FanOutNotFoundException;
import com.example.websocketdemo.Exceptions.GroupNotFoundException;
import com.example.websocketdemo.Exceptions.UsernameAlreadyExistException;
import com.example.websocketdemo.Exceptions.userNotFoundException;
import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.PrivateChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.Security.TokenValidator;
import com.example.websocketdemo.config.AWSConfig;
import com.example.websocketdemo.model.*;
import com.google.gson.Gson;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PrivateChatServices {
    private final ChatRepo chatRepo;
    private final GroupChatRepo groupChatRepo;
    private final UserRepo userRepo;
    private final AmqpAdmin amqpAdmin;
    private final RabbitManagementTemplate rabbitManagementTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenValidator tokenValidator;
    private final Environment environment;
    private final PrivateChatRepo privateChatRepo;
    private Map<String, WebSocketSession> allSessions = new HashMap<>();

    public PrivateChatServices(ChatRepo chatRepo, GroupChatRepo groupChatRepo, UserRepo userRepo,
                               AmqpAdmin amqpAdmin, RabbitManagementTemplate rabbitManagementTemplate, RabbitTemplate rabbitTemplate,
                               BCryptPasswordEncoder bCryptPasswordEncoder, AuthenticationManager authenticationManager,
                               TokenValidator tokenValidator, Environment environment, PrivateChatRepo privateChatRepo) {
        this.chatRepo = chatRepo;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
        this.amqpAdmin = amqpAdmin;
        this.rabbitManagementTemplate = rabbitManagementTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenValidator = tokenValidator;
        this.environment = environment;
        this.privateChatRepo = privateChatRepo;
    }


    public ChatUser getUser(String username) {
        if (userRepo.findByUsername(username).isPresent()) {
            return userRepo.findByUsername(username).get();
        }
        return null;
    }



    public ChatUser createUser(ChatUser user) {
        Optional<ChatUser> repetitive = userRepo.findByUsername(user.getUsername());
        if (repetitive.isPresent()) throw new UsernameAlreadyExistException("Sorry but this username is taken");
        System.out.println(environment.getProperty("bucket.name"));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public void deleteUser(String id) {
        userRepo.deleteById(id);
    }



    public String SendToken(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username, password
                )
        );
        System.out.println(authentication);
        return "bearer " + tokenValidator.generateToken(authentication);

    }




    public void sendingAllQueuedMessagesToUser(WebSocketSession session) throws InterruptedException {
        Gson gson = new Gson();
        String username = Objects.requireNonNull(session.getUri()).toString().substring(4);
        if (rabbitManagementTemplate.getQueue("user." + username) != null) {
            List<String> allMessages = new ArrayList<>();
            int numberOfMessages = (int) amqpAdmin.getQueueProperties("user." + username).get("QUEUE_MESSAGE_COUNT");
            if (numberOfMessages > 0) {
                for (int i = 0; i < numberOfMessages; i++) {
                    byte[] binary = rabbitTemplate.receive("user." + username).getBody();
                    String Smesssage = new String(binary, StandardCharsets.UTF_8);
                    allMessages.add(Smesssage);
                }
                try {
                    session.sendMessage(new TextMessage(gson.toJson(allMessages)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void addMessageToQueue(String message) {
        Gson gson = new Gson();
        ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
        String exchange = compareNamesAlphabetically(chatMessage.getSender(), chatMessage.getReceiver());
        if (rabbitManagementTemplate.getExchange(exchange) != null) {
            rabbitTemplate.convertAndSend(exchange, "", chatMessage);
            if (allSessions.get(chatMessage.getReceiver()) != null) {
                WebSocketSession session = allSessions.get(chatMessage.getReceiver());
                WebSocketSession senderSession = null;
                if (allSessions.get(chatMessage.getSender()) != null) {
                    senderSession = allSessions.get(chatMessage.getSender());
                }
                try {
                    sendingAllQueuedMessagesToUser(session);
                    if (senderSession != null) {
                        sendingAllQueuedMessagesToUser(senderSession);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }



    // in future we can use Elastic Cache Redis from aws to store all the sessions
    public void savingUserSessions(WebSocketSession session) {
        String username = Objects.requireNonNull(session.getUri()).toString().substring(4);
        allSessions.put(username, session);
        System.out.println("all active users" + allSessions);
    }

    public void deletingUserFromSessions(WebSocketSession session) {
        String username = Objects.requireNonNull(session.getUri()).toString().substring(4);
        allSessions.remove(username, session);
        System.out.println(allSessions);
    }



    public List<ChatUser> getAll() {
        return userRepo.findAll();
    }




    public void createQueuesAndExchangeForPrivateChat(String message) {
        // Alternate exchange is when a message doesnt have a proper key and there fore exchanges
        // like topic and direct cant publish it there fore we use an alternate
        // exchange which is fanout to be a publisher of those messages
        Gson gson = new Gson();
        ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);

        String exchangeName;
        exchangeName = compareNamesAlphabetically(chatMessage.getSender(), chatMessage.getReceiver());
        if (rabbitManagementTemplate.getExchange(exchangeName) == null) {
            FanoutExchange exchange = new FanoutExchange(exchangeName, false, true, null);

            FanoutExchange deadLetterSender = new FanoutExchange("dead-letter-" + chatMessage.getSender(), false, true, null);
            FanoutExchange deadLetterGir = new FanoutExchange("dead-letter-" + chatMessage.getReceiver(), false, true, null);

            Map<String, Object> argsGir = new HashMap<>();
            argsGir.put("x-dead-letter-exchange", "dead-letter-" + chatMessage.getReceiver());
            argsGir.put("x-message-ttl", 360000000);

            Map<String, Object> argsSender = new HashMap<>();
            argsSender.put("x-dead-letter-exchange", "dead-letter-" + chatMessage.getReceiver());
            argsSender.put("x-message-ttl", 360000000);


            Queue girande = new Queue("user." + chatMessage.getReceiver(), false, false,
                    true, argsGir);
            Queue sending = new Queue("user." + chatMessage.getSender(), false, false,
                    true, argsSender);

            // Queue for dead letter exchange
            Queue deadQueueSender = new Queue("deadQueue" + "_" + chatMessage.getSender(), false, false,
                    true, null);
            Queue deadQueueGir = new Queue("deadQueue" + "_" + chatMessage.getReceiver(), false, false,
                    true, null);

            //Binding all the queues to its exchanges to be ready to be declared
            Binding first = BindingBuilder.bind(sending).to(exchange);
            Binding second = BindingBuilder.bind(girande).to(exchange);
            Binding third = BindingBuilder.bind(deadQueueSender).to(deadLetterSender);
            Binding fourth = BindingBuilder.bind(deadQueueGir).to(deadLetterGir);

            List<Object> all = Arrays.asList(exchange, deadLetterSender, deadLetterGir, sending, girande,
                    deadQueueSender, deadQueueGir, first, second, third, fourth);
            all.forEach(i -> {
                if (i.getClass().toString().endsWith("Queue")) {
                    Queue computedQueue = (Queue) i;
                    if (rabbitManagementTemplate.getQueue(computedQueue.getName()) == null) {
                        amqpAdmin.declareQueue((Queue) i);
                    }
                } else if (i.getClass().toString().endsWith("Exchange")) {
                    amqpAdmin.declareExchange((Exchange) i);

                } else if (i.getClass().toString().endsWith("Binding")) {
                    amqpAdmin.declareBinding((Binding) i);
                }
            });
        }

    }

    public String compareNamesAlphabetically(String name, String name1) {
        String fullName;
        if (name.compareTo(name1) > 0) {
            fullName = name + "_" + name1;
        } else {
            fullName = name1 + "_" + name;
        }
        return fullName;
    }

    public boolean CheckAvailability(String name) {
        Exchange exchange = rabbitManagementTemplate.getExchange(name);
        return exchange == null;
    }

    public void sendErrorMessageToUser(String username) {
        FanoutExchange errorEx = new FanoutExchange("errorEx", false, false, null);
        Queue user = rabbitManagementTemplate.getQueue("user." + username);
        amqpAdmin.declareExchange(errorEx);
        Binding errorBind = BindingBuilder.bind(user).to(errorEx);
        amqpAdmin.declareBinding(errorBind);
        ChatMessage chatMessage1 = new ChatMessage();
        chatMessage1.setTextContent("there is an error");
        chatMessage1.setContentType("error");
        rabbitTemplate.convertAndSend("errorEx", "", chatMessage1);
        rabbitManagementTemplate.deleteExchange(errorEx);
    }

    public void createPrivateChat(ChatUser user1,ChatUser user2){
        PrivateChat privateChat= new PrivateChat();
        List<String> users= privateChat.getUsers();
        users.add(user1.getUsername());
        users.add(user2.getUsername());
        privateChat.setUsers(users);
        privateChatRepo.save(privateChat);
        Optional<PrivateChat> pr= privateChatRepo.findByUsers(users);
        pr.ifPresent(System.out::println);
    }

    public void saveMessage(TextMessage message)  {
        ChatMessage chatMessage = gettingMessageFromSocket(message);
        Optional<ChatUser> optionSender =userRepo.findByUsername(chatMessage.getSender());
        Optional<ChatUser> optionReceiver = userRepo.findByUsername(chatMessage.getReceiver());

        if(optionSender.isPresent() && optionReceiver.isPresent()){
            ChatUser sender = optionSender.get();
            ChatUser receiver = optionReceiver.get();
            System.out.println(sender);
            List<String> users= new ArrayList<>();
            users.add(chatMessage.getSender());
            users.add(chatMessage.getReceiver());
            Optional<PrivateChat> optionPrivateChat= privateChatRepo.findByUsers(users);

            if(optionPrivateChat.isPresent()){
                    PrivateChat privateChat = optionPrivateChat.get();
                System.out.println(privateChat);
                    List<ChatMessage> allMessages=privateChat.getMessages();
                    allMessages.add(chatMessage);
                    privateChat.setMessages(allMessages);
                    privateChatRepo.save(privateChat);

            }else{
                createPrivateChat(sender,receiver);
                saveMessage(message);
            }

        }

    }

    public ChatMessage gettingMessageFromSocket(TextMessage message) {
        Gson gson = new Gson();
        return gson.fromJson(message.getPayload(), ChatMessage.class);

    }

}













