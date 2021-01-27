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
public class ChatServices {
    private final ChatRepo chatRepo;
    private final GroupChatRepo groupChatRepo;
    private final UserRepo userRepo;
    private final AmqpAdmin amqpAdmin;
    private final RabbitManagementTemplate rabbitManagementTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenValidator tokenValidator;
    private final AWSConfig awsConfig;
    private final Environment environment;
    private final Regions regions=Regions.US_WEST_2;
    private final PrivateChatRepo privateChatRepo;
    private Map<String,WebSocketSession> allSessions=new HashMap<>();

    public ChatServices(ChatRepo chatRepo, GroupChatRepo groupChatRepo, UserRepo userRepo,
                        AmqpAdmin amqpAdmin, RabbitManagementTemplate rabbitManagementTemplate, RabbitTemplate rabbitTemplate, BCryptPasswordEncoder bCryptPasswordEncoder, AuthenticationManager authenticationManager, TokenValidator tokenValidator, AWSConfig awsConfig, Environment environment, PrivateChatRepo privateChatRepo) {
        this.chatRepo = chatRepo;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
        this.amqpAdmin = amqpAdmin;
        this.rabbitManagementTemplate = rabbitManagementTemplate;
        this.rabbitTemplate = rabbitTemplate;
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
        Optional<ChatUser> repetitive=userRepo.findByUsername(user.getUsername());
        if(repetitive.isPresent())throw new UsernameAlreadyExistException("Sorry but this username is taken");
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


    public void sendingAllQueuedMessagesToUser(WebSocketSession session) throws InterruptedException {
        Gson gson = new Gson();
        String username = Objects.requireNonNull(session.getUri()).toString().substring(4);
        if (rabbitManagementTemplate.getQueue("user." + username) != null) {
            List<String> allMessages = new ArrayList<>();
            int numberOfMessages = (int) amqpAdmin.getQueueProperties("user." + username).get("QUEUE_MESSAGE_COUNT");
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
        }else{
            System.out.println("no message for this user"+username);
        }
    }

    public void addMessageToQueue(String message) {
        Gson gson = new Gson();
        ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
        String exchange = compareNamesAlphabetically(chatMessage.getSender(), chatMessage.getReceiver());
        if (rabbitManagementTemplate.getExchange(exchange) != null) {
            rabbitTemplate.convertAndSend(exchange, "", chatMessage);
            if (allSessions.get(chatMessage.getReceiver()) != null) {
                System.out.println("receiver exists");
                WebSocketSession session = allSessions.get(chatMessage.getReceiver());
                WebSocketSession senderSession = null;
                if (allSessions.get(chatMessage.getSender())!=null){
                    senderSession=allSessions.get(chatMessage.getSender());
                }
                try {
                    sendingAllQueuedMessagesToUser(session);
                    if (senderSession != null) {
                        sendingAllQueuedMessagesToUser(senderSession);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("user  does not exist ");
            }

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

        // in future we can use Elastic Cache Redis from aws to store all the sessions
    public void savingUserSessions(WebSocketSession session) {
        String username = Objects.requireNonNull(session.getUri()).toString().substring(4);
        allSessions.put(username,session);
        System.out.println(allSessions);
    }
    public void deletingUserFromSessions(WebSocketSession session){
        String username = Objects.requireNonNull(session.getUri()).toString().substring(4);
        allSessions.remove(username,session);
        System.out.println(allSessions);
    }
    public void sendPrivateMessage(ChatMessage chatMessage, SimpMessageHeaderAccessor simpMessageHeaderAccessor){
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

    public void createQueue(String sender, RabbitTools rabbitTools){

    }
    public void createQueuesAndExchangeForPrivateChat(String message) {
        // Alternate exchange is when a message doesnt have a proper key and there fore exchanges
        // like topic and direct cant publish it there fore we use an alternate
        // exchange which is fanout to be a publisher of those messages
            Gson gson= new Gson();
            ChatMessage chatMessage=gson.fromJson(message,ChatMessage.class);

            String exchangeName;
            exchangeName = compareNamesAlphabetically(chatMessage.getSender(),chatMessage.getReceiver());
            if(rabbitManagementTemplate.getExchange(exchangeName)==null){
                FanoutExchange exchange = new FanoutExchange(exchangeName, false, true, null);

                FanoutExchange deadLetterSender = new FanoutExchange("dead-letter-" + chatMessage.getSender(), false, true, null);
                FanoutExchange deadLetterGir = new FanoutExchange("dead-letter-" +chatMessage.getReceiver(), false, true, null);

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
                        Queue computedQueue= (Queue) i;
                        if(rabbitManagementTemplate.getQueue(computedQueue.getName())==null){
                            amqpAdmin.declareQueue((Queue) i);
                        }
                    } else if (i.getClass().toString().endsWith("Exchange")) {
                        amqpAdmin.declareExchange((Exchange) i);

                    } else if (i.getClass().toString().endsWith("Binding")) {
                        amqpAdmin.declareBinding((Binding) i);
                    }
                });
            }else{
                System.out.println("No reason to create");
            }



//        System.out.println(amqpAdmin.getQueueProperties("user." + sender));


//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setUri("ampq://guest:guest@localhost:61613/virtualHost");
//        Connection conn= factory.newConnection();
//        Channel channel= conn.createChannel();
        //this way i can  close a channel and do some things other than adminMq

    }


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

    public boolean checkAuthorization(WebSocketSession session){
        return (boolean) session.getAttributes().get("authorization");
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
    @Async
    public void uploadingToS3(){
        String bucket = "advancednodejs";
        String key="amir.iso";
        File file= new File("/Users/amirsayyar/Downloads/ubuntu.iso");
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
    public void downloadFromS3() {
        System.out.println(416);
        String bucket = "advancednodejs";
        TransferManager transferManager = awsConfig.creatingTransferManager();
        ProgressListener progressListener = progressEvent -> System.out.println(progressEvent.getBytesTransferred());
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket,"amir.iso");
        getObjectRequest.setGeneralProgressListener(progressListener);
        Download download = transferManager.download(getObjectRequest, new File("/Users/amirsayyar/Desktop/amir.iso"));

        try {
            download.wait();
            if(download.isDone()) System.out.println("its done brother");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        S3Object s3Object = awsConfig.creatClient().getObject(bucket, key);
//        S3ObjectInputStream stream = s3Object.getObjectContent();
//        try {
////            byte[] content = IOUtils.toByteArray(stream);
////            ByteArrayResource arrayResource = new ByteArrayResource(content);
////            System.out.println(arrayResource.contentLength()); // Content-Type:application/Octet-stream
////            FileOutputStream outputStream = new FileOutputStream("/Users/amirsayyar/Desktop/salam.iso");
////            outputStream.write(stream.getDelegateStream().readAllBytes());
//        } catch (IOException e) {
//            System.out.println(e.getLocalizedMessage());
//
//        }
    }
}

//    public ResponseEntity<byte []> prepareContent(String range) throws IOException {
//        long rangeStart=0;
//        long rangeEnd;
//        byte[] data;
//        long fileSize=0;
//        File file= new File("/Users/amirsayyar/Downloads/ubuntu.iso");
//
//            fileSize= Files.size(Path.of("/Users/amirsayyar/Downloads/ubuntu.iso"));
//
//        if(range==null){
//            return ResponseEntity.ok()
//                    .header("Content-Type","video/mp4")
//                    .header("Content-Length",String.valueOf(fileSize))
//                    .body(readByteRange(rangeStart,500000));
//        }
//        String[] ranges= range.split("_");
//        rangeStart=Long.parseLong(ranges[0].substring(0));
//        if(ranges.length>1){
//            rangeEnd=Long.parseLong(ranges[1]);
//        }else {
//            rangeEnd=fileSize-1;
//        }if(rangeEnd>fileSize){
//            rangeEnd=fileSize-1;
//        }
//        data= readByteRange(rangeStart,rangeEnd);
//        String contentLength=String.valueOf((rangeEnd-rangeStart)+1);
//        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
//                .header("Content-Type","application/octet-stream")
//                .header("Accept-Ranges","bytes")
//                .header("Content-Length",contentLength)
//                .header("Content-Range","bytes"+" "+rangeStart+"-"+rangeEnd+"/"+fileSize)
//                .body(data);
//
//    }
//
//
//    public byte[] readByteRange( long start ,long end){
//        File file = new File("/Users/amirsayyar/Downloads/ubuntu.iso");
//        try (InputStream inputStream = new FileInputStream(file);
//             ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream()) {
//            byte[] data = new byte[1024];
//            int nRead;
//            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
//                bufferedOutputStream.write(data, 0, nRead);
//            }
//            bufferedOutputStream.flush();
//            byte[] result = new byte[(int) (end - start) + 1];
//            System.arraycopy(bufferedOutputStream.toByteArray(), (int) start, result, 0, result.length);
//            return result;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return new byte[0];
//    }









