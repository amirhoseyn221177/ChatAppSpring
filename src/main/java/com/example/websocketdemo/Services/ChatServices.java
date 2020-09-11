package com.example.websocketdemo.Services;

import com.amazonaws.HttpMethod;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.IOUtils;
import com.example.websocketdemo.Exceptions.FanOutNotFoundException;
import com.example.websocketdemo.Exceptions.GroupNotFoundException;
import com.example.websocketdemo.Exceptions.userNotFoundException;
import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.PrivateChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.Security.TokenValidator;
import com.example.websocketdemo.config.AWSConfig;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import com.example.websocketdemo.model.PrivateChat;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatServices {
    private final ChatRepo chatRepo;
    private final GroupChatRepo groupChatRepo;
    private final UserRepo userRepo;
    private final AmqpAdmin amqpAdmin;
    private final RabbitManagementTemplate rabbitManagementTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenValidator tokenValidator;
    private final AWSConfig awsConfig;
    private final Environment environment;
    private final Regions regions=Regions.US_WEST_2;
    private final PrivateChatRepo privateChatRepo;

    public ChatServices(ChatRepo chatRepo, GroupChatRepo groupChatRepo, UserRepo userRepo,
                        AmqpAdmin amqpAdmin, RabbitManagementTemplate rabbitManagementTemplate, RabbitTemplate rabbitTemplate, SimpMessagingTemplate simpMessagingTemplate, BCryptPasswordEncoder bCryptPasswordEncoder, AuthenticationManager authenticationManager, TokenValidator tokenValidator, AWSConfig awsConfig, Environment environment, PrivateChatRepo privateChatRepo) {
        this.chatRepo = chatRepo;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
        this.amqpAdmin = amqpAdmin;
        this.rabbitManagementTemplate = rabbitManagementTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenValidator = tokenValidator;
        this.awsConfig = awsConfig;
        this.environment = environment;
        this.privateChatRepo = privateChatRepo;
    }


    public ChatUser getUser(String username) {
        if (userRepo.findByUsername(username).isPresent()) {
            return userRepo.findByUsername(username).get();
        }
        return null;
    }

    public GroupChat createGroupChat(String username, String groupName) {
        Optional<ChatUser> founder = userRepo.findByUsername(username);
        GroupChat groupChat = new GroupChat();
        groupChat.setName(groupName);
        groupChat.getAdmins().add(founder.map(ChatUser::getId).orElse(null));
        groupChat.getMembers().add(founder.map(ChatUser::getId).orElse(null));
        String id = groupChatRepo.save(groupChat).getId();
        founder.ifPresent(chatUser -> chatUser.getGroupChats().add(id));
        founder.ifPresent(userRepo::save);
        return groupChat;
    }

    public ChatUser createUser(ChatUser user) {
        System.out.println(environment.getProperty("bucket.name"));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public void deleteUser(String id) {
        userRepo.deleteById(id);
    }

    public GroupChat addToGroupChat(String groupId, String username) {
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        System.out.println(74);
        if (groupChat.isPresent()) {
            System.out.println(groupChat.get());
            List<String> allUsers = groupChat.get().getMembers();
            Optional<ChatUser> user1 = userRepo.findByUsername(username);
            System.out.println(user1);
            if (user1.isEmpty()) throw new userNotFoundException("there is no such a user");
            allUsers.add(user1.map(ChatUser::getId).orElse(null));
            groupChat.get().setMembers(allUsers);
            String id = groupChatRepo.save(groupChat.get()).getId();
            user1.ifPresent(user2 -> user2.getGroupChats().add(id));
            user1.ifPresent(userRepo::save);
            AtomicReference<GroupChat> returnGroup = new AtomicReference<>(new GroupChat());
            groupChatRepo.findById(id).ifPresent(returnGroup::set);
            return returnGroup.get();
        } else {
            throw new GroupNotFoundException("there is no such a group");

        }
    }

    public String SendToken(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username, password
                )
        );
        return "bearer " + tokenValidator.generateToken(authentication);

    }

    public void deleteGroupChat(String groupId, String username) {
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        if (groupChat.isPresent()) {
            List<String> admins = groupChat.get().getAdmins();
            Optional<ChatUser> sender = userRepo.findByUsername(username);
            if (sender.isPresent()) {
                if (admins.contains(sender.get().getId())) {
                    List<String> members = groupChat.get().getMembers();
                    members.forEach(m -> {
                        Optional<ChatUser> member = userRepo.findById(m);
                        member.ifPresent(mem -> {
                            List<String> groupIDS = mem.getGroupChats();
                            groupIDS.removeIf(id -> id.equals(groupId));
                        });
                    });
                    groupChatRepo.deleteById(groupId);
                    System.out.println("groupChat deleted");
                } else {
                    throw new userNotFoundException("this user doesn't have the authority");
                }
            } else {
                throw new userNotFoundException("there is no such a user");
            }
        } else {
            throw new GroupNotFoundException("there is no such a group chat");
        }

    }

    public void broadCastMessageToGroupChat(ChatMessage chatMessage, String groupId) {
        System.out.println(125);
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        AtomicReference<String> name = new AtomicReference<>("");
        groupChat.ifPresent(groupChat1 -> {
            System.out.println("groupChat exists");
            Optional<ChatUser> chatUser = userRepo.findByUsername(chatMessage.getSender());
            if (chatUser.isPresent()) {
                System.out.println(123);
                System.out.println(chatUser.get().getGroupChats());
                System.out.println(groupId);
                if (chatUser.get().getGroupChats().contains(groupId)) {
                    System.out.println("user in groupChat");
                    name.set(groupChat1.getName());
                    System.out.println(chatMessage);
                    if (chatMessage.getContentType().equals("text") ||
                            chatMessage.getContentType().equals("link")) {
                        List<String> texts = groupChat.get().getTexts();
                        texts.add(chatMessage.getTextContent());
                        groupChat.get().setTexts(texts);
                    } else {
                        List<String> medias = groupChat.get().getMedias();
                        medias.add(chatMessage.getMediaContent());
                        groupChat.get().setMedias(medias);
                    }
                    groupChatRepo.save(groupChat.get());
                    rabbitTemplate.convertAndSend("G_" + name, "", chatMessage);
                }
            }

        });
    }

    public void savePrivateMessage(ChatMessage chatMessage,PrivateChat  privateChat){
            if(chatMessage.getContentType().equals("media")){
                List<String> medias=privateChat.getMedias();
                medias.add(chatMessage.getMediaContent());
                privateChat.setMedias(medias);
            }else {
                List<String> texts= privateChat.getTexts();
                texts.add(chatMessage.getTextContent());
                privateChat.setTexts(texts);
            }
            privateChatRepo.save(privateChat);
        }



    public void sendPrivateMessage(ChatMessage chatMessage, SimpMessageHeaderAccessor simpMessageHeaderAccessor){
//        Optional<PrivateChat> OPprivateChat=privateChatRepo.findById(chatId);
        List<String> users=new ArrayList<>();
        users.add(chatMessage.getReceiver());
        users.add(chatMessage.getSender());
        Optional<PrivateChat> OPprivateChat=privateChatRepo.findByUsers(users);
        OPprivateChat.ifPresent(privateChat -> savePrivateMessage(chatMessage, privateChat));
        try{
            System.out.println(chatMessage.getSender());
            List<?> name = (List<?>) Objects.requireNonNull(simpMessageHeaderAccessor.getSessionAttributes()).get("exchangeName");
            if(CheckAvailability((String)name.get(0))){
                System.out.println(76);
                sendErrorMessageToUser(chatMessage.getSender());
                throw new FanOutNotFoundException("Error has occurred");
            }
            rabbitTemplate.convertAndSend((String) name.get(0), "", chatMessage);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public List<ChatUser> getAll() {
        return userRepo.findAll();
    }

    public List<GroupChat> getAllGroups() {
        return groupChatRepo.findAll();
    }

    public GroupChat getGroup(String groupId) {
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        return groupChat.orElse(null);
    }

    public void createQueuesAndExchangeForPrivateChat(String sender, String receiver) {
        // Alternate exchange is when a message doesnt have a proper key and there fore exchanges
        // like topic and direct cant publish it there fore we use an alternate
        // exchange which is fanout to be a publisher of those messages

        String exchangeName;
        exchangeName = compareNamesAlphabetically(sender, receiver);
        FanoutExchange exchange = new FanoutExchange(exchangeName, false, true, null);

        FanoutExchange deadLetterSender = new FanoutExchange("dead-letter-" + sender, false, true, null);
        FanoutExchange deadLetterGir = new FanoutExchange("dead-letter-" + receiver, false, true, null);

        Map<String, Object> argsGir = new HashMap<>();
        argsGir.put("x-dead-letter-exchange", "dead-letter-" + receiver);
        argsGir.put("x-message-ttl", 360000000);

        Map<String, Object> argsSender = new HashMap<>();
        argsSender.put("x-dead-letter-exchange", "dead-letter-" + sender);
        argsSender.put("x-message-ttl", 360000000);


        Queue girande = new Queue("user." + receiver, false, false, true, argsGir);
        Queue sending = new Queue("user." + sender, false, false, true, argsSender);

        // Queue for dead letter exchange
        Queue deadQueueSender = new Queue("deadQueue" + "_" + sender, false, false, true, null);
        Queue deadQueueGir = new Queue("deadQueue" + "_" + receiver, false, false, true, null);

        //Binding all the queues to its exchanges to be ready to be declared
        Binding first = BindingBuilder.bind(sending).to(exchange);
        Binding second = BindingBuilder.bind(girande).to(exchange);
        Binding third = BindingBuilder.bind(deadQueueSender).to(deadLetterSender);
        Binding fourth = BindingBuilder.bind(deadQueueGir).to(deadLetterGir);

        List<Object> all = Arrays.asList(exchange, deadLetterSender, deadLetterGir, sending, girande,
                deadQueueSender, deadQueueGir, first, second, third, fourth);

        all.forEach(i -> {
            if (i.getClass().toString().endsWith("Queue")) {
                amqpAdmin.declareQueue((Queue) i);
            } else if (i.getClass().toString().endsWith("Exchange")) {
                amqpAdmin.declareExchange((Exchange) i);

            } else if (i.getClass().toString().endsWith("Binding")) {
                amqpAdmin.declareBinding((Binding) i);
            }
        });
//        System.out.println(amqpAdmin.getQueueProperties("user." + sender));


//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setUri("ampq://guest:guest@localhost:61613/virtualHost");
//        Connection conn= factory.newConnection();
//        Channel channel= conn.createChannel();
        //this way i can  close a channel and do some things other than adminMq

    }
//
//    public ChatMessage gettingMessagesFromQueues(String name){
//        return (ChatMessage) rabbitTemplate.receiveAndConvert(name);
//    }
//
//    public void sendingMessageToExchange(ChatMessage chatMessage , String sender , String receiver){
//        String exchangeName;
//        exchangeName=compareNamesAlphabetically(sender,receiver);
//        rabbitTemplate.convertAndSend(exchangeName,"",chatMessage);
//    }

    public void createQueuesAndExchangeForGroupChat(String groupId, String sender) {
        System.out.println(240);
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        groupChat.ifPresent(groupChat1 -> {
            System.out.println(243);
            String exchange = "G_" + groupChat1.getName();
            FanoutExchange group = new FanoutExchange(exchange, false, true, null);
            List<String> chatUsers = groupChat1.getMembers();
            amqpAdmin.declareExchange(group);
            for (String id : chatUsers) {
                Optional<ChatUser> user = userRepo.findById(id);
                user.ifPresent(u -> {
                    if (rabbitManagementTemplate.getQueue("user." + u.getUsername()) != null) {
                        System.out.println(252);
                        Queue userQueue = rabbitManagementTemplate.getQueue("user." + u.getUsername());
                        Binding using = BindingBuilder.bind(userQueue).to(group);
                        amqpAdmin.declareBinding(using);
                    } else {
                        Map<String, Object> args = new HashMap<>();
                        args.put("x-dead-letter-exchange", "dead-letter-" + u.getUsername());
                        System.out.println(258);
                        args.put("x-message-ttl", 360000000);
                        Queue using = new Queue("user." + u.getUsername(), false, false, true, args);
                        Binding userBind = BindingBuilder.bind(using).to(group);
                        amqpAdmin.declareQueue(using);
                        amqpAdmin.declareBinding(userBind);
                    }
                });
            }
        });
        if (groupChat.isEmpty()) {
            System.out.println(267);
            FanoutExchange error = new FanoutExchange("error", false, false, null);
            Queue errorQ = rabbitManagementTemplate.getQueue("user." + sender);
            amqpAdmin.declareExchange(error);
            Binding b = BindingBuilder.bind(errorQ).to(error);
            amqpAdmin.declareBinding(b);
            rabbitTemplate.convertAndSend("error", "", "this group chat doesnt exist");
            amqpAdmin.deleteExchange("error");
            amqpAdmin.deleteQueue("user." + sender);
        }
        if (userRepo.findByUsername(sender).isEmpty()) {
            FanoutExchange error = new FanoutExchange("error", false, false, null);
            Queue errorQ = rabbitManagementTemplate.getQueue("user." + sender);
            amqpAdmin.declareExchange(error);
            Binding b = BindingBuilder.bind(errorQ).to(error);
            amqpAdmin.declareBinding(b);
            rabbitTemplate.convertAndSend("error", "", "this user doesnt exist");
            amqpAdmin.deleteExchange("error");
            amqpAdmin.deleteQueue("user." + sender);
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
        Queue user = rabbitManagementTemplate.getQueue("user."+username);
        amqpAdmin.declareExchange(errorEx);
        Binding errorBind = BindingBuilder.bind(user).to(errorEx);
        amqpAdmin.declareBinding(errorBind);
        ChatMessage chatMessage1 = new ChatMessage();
        chatMessage1.setTextContent("there is an error");
        chatMessage1.setContentType("error");
        rabbitTemplate.convertAndSend("errorEx", "", chatMessage1);
        rabbitManagementTemplate.deleteExchange(errorEx);
    }

    public boolean receiverExist(String sender, String receiver) {
        Optional<ChatUser> chatUser = userRepo.findByUsername(receiver);
        if (chatUser.isEmpty()) {
            sendErrorMessageToUser(sender);
            return false;
        }
        return true;
    }


    public URL gettingPreSigned(){
        AmazonS3 client=awsConfig.creatClient();
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        String bucket = "advancednodejs";
        GeneratePresignedUrlRequest preSigned= new GeneratePresignedUrlRequest(bucket,"amir2211", HttpMethod.PUT)
                .withExpiration(expiration);
//                .withContentType("image/*");
        URL url = client.generatePresignedUrl(preSigned);
        System.out.println(url);
        return url;
    }

    // not as useful since i want to do it in front end
    public void uploadingToS3(String path){
        String bucket = "advancednodejs";
        String key="amir.png";
        File file= new File("/home/amir/Downloads/trans.png");
        TransferManager tm  = awsConfig.creatingTransferManager();

        //Progress listener is for seeing how much byte we are sending
        ProgressListener progressListener = progressEvent -> System.out.println(
                "Transferred bytes: " + progressEvent.getBytesTransferred());
        PutObjectRequest request = new PutObjectRequest(
                bucket, key, file);
        request.setGeneralProgressListener(progressListener);
        Upload upload = tm.upload(request);
        try {
            upload.waitForCompletion();
        } catch (InterruptedException e) {
            System.out.println(e.getLocalizedMessage());;
        }

    }


    //downloading from a bucket
    @Async
    public void downloadFromS3(){
        System.out.println(416);
        String bucket = "advancednodejs";
        String key="amir.png";
        TransferManager transferManager=awsConfig.creatingTransferManager();
        ProgressListener progressListener=progressEvent -> {
            System.out.println(progressEvent.getBytesTransferred());
        };

        S3Object s3Object= awsConfig.creatClient().getObject(bucket,key);
        S3ObjectInputStream stream=s3Object.getObjectContent();
        try {
            byte [] content= IOUtils.toByteArray(stream);
            ByteArrayResource arrayResource= new ByteArrayResource(content);
            System.out.println(arrayResource.contentLength()); // content type :application/Octet-stream
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }


}
