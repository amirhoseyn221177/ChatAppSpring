package com.example.websocketdemo.Services;

import com.example.websocketdemo.Exceptions.GroupNotFoundException;
import com.example.websocketdemo.Exceptions.UserNotFoundResponse;
import com.example.websocketdemo.Exceptions.userNotFoundException;
import com.example.websocketdemo.Repository.ChatRepo;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

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

    public ChatServices(ChatRepo chatRepo, GroupChatRepo groupChatRepo, UserRepo userRepo,
                        AmqpAdmin amqpAdmin, RabbitManagementTemplate rabbitManagementTemplate, RabbitTemplate rabbitTemplate) {
        this.chatRepo = chatRepo;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
        this.amqpAdmin = amqpAdmin;
        this.rabbitManagementTemplate = rabbitManagementTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }


    public ChatUser getUser(String username){
        if(userRepo.findByUsername(username).isPresent()){
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

    public void createUser(ChatUser user) {
        userRepo.save(user);
    }

    public void deleteUser(String id) {
        userRepo.deleteById(id);
    }

    public GroupChat addToGroupChat(String groupId, String username) {
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        System.out.println(74);
            if(groupChat.isPresent()) {
                System.out.println(groupChat.get());
                List<String> allUsers = groupChat.get().getMembers();
                Optional<ChatUser> user1 = userRepo.findByUsername(username);
                System.out.println(user1);
                if (!user1.isPresent()) throw new userNotFoundException("there is no such a user");
                allUsers.add(user1.map(ChatUser::getId).orElse(null));
                groupChat.get().setMembers(allUsers);
                String id = groupChatRepo.save(groupChat.get()).getId();
                user1.ifPresent(user2 -> user2.getGroupChats().add(id));
                user1.ifPresent(userRepo::save);
                AtomicReference<GroupChat> returnGroup = new AtomicReference<>(new GroupChat());
                groupChatRepo.findById(id).ifPresent(returnGroup::set);
                return returnGroup.get();
            }else {
                throw new GroupNotFoundException("there is no such a group");

            }


    }

    public void deleteGroupChat(String groupId, ChatUser chatUser) {
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        if (groupChat.isPresent()) {
            List<String> admins = groupChat.get().getAdmins();
            Optional<ChatUser> sender =userRepo.findByUsername(chatUser.getUsername());
            if (sender.isPresent()) {
                if(admins.contains(sender.get().getId())){
                    List<String > members = groupChat.get().getMembers();
                    members.forEach(m -> {
                        Optional<ChatUser> member = userRepo.findById(m);
                        member.ifPresent(mem ->{
                            List<String> groupIDS=mem.getGroupChats();
                            groupIDS.removeIf(id->id.equals(groupId));
                        });
                    });
                    groupChatRepo.deleteById(groupId);
                    System.out.println("groupChat deleted");
                }else {
                    throw new userNotFoundException("there is no such a user");
                }
            }else {
                throw new userNotFoundException("this user doesn't have the authority");
            }
        }else {
            throw new GroupNotFoundException("there is no such a group chat");
        }

    }

    public void broadCastMessageToGroupChat(ChatMessage chatMessage, String groupId) {
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        String name = "";
        if (groupChat.isPresent()) {
            System.out.println("groupChat exists");
            Optional<ChatUser> chatUser = userRepo.findByUsername(chatMessage.getSender());
            if (chatUser.isPresent()) {
                System.out.println(123);
                System.out.println(chatUser.get().getGroupChats());
                System.out.println(groupId);
                if (chatUser.get().getGroupChats().contains(groupId)) {
                    System.out.println("user in groupChat");
                    name = groupChat.get().getName();
                    System.out.println(chatMessage);
                    if (chatMessage.getContentType().equals("text") ||
                            chatMessage.getContentType().equals("link")) {
                        List<String> texts = groupChat.get().getTexts();
                        texts.add(chatMessage.getTextContent());
                        groupChat.get().setTexts(texts);
                    } else {
                        List<byte[]> medias = groupChat.get().getMedias();
                        medias.add(chatMessage.getMediaContent());
                        groupChat.get().setMedias(medias);
                    }
                    groupChatRepo.save(groupChat.get());
                    rabbitTemplate.convertAndSend("G_" + name, "", chatMessage);
                }
            }

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
        argsGir.put("x-message-ttl", 3600000);

        Map<String, Object> argsSender = new HashMap<>();
        argsSender.put("x-dead-letter-exchange", "dead-letter-" + sender);
        argsSender.put("x-message-ttl", 3600000);


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
        System.out.println(amqpAdmin.getQueueProperties("user." + sender));


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

    public void createQueuesAndExchangeForGroupChat(String groupId) {
        System.out.println(230);
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        if (groupChat.isPresent()) {
            String exchange = "G_" + groupChat.get().getName();
            FanoutExchange group = new FanoutExchange(exchange, false, true, null);
            List<String> chatUsers = groupChat.get().getMembers();
            amqpAdmin.declareExchange(group);
            for (String id : chatUsers) {
                Optional<ChatUser> user=userRepo.findById(id);
                user.ifPresent(u->{
                    if (rabbitManagementTemplate.getQueue("user." + u.getUsername()) != null) {
                        Queue userQueue = rabbitManagementTemplate.getQueue("user." + u.getUsername());
                        Binding using = BindingBuilder.bind(userQueue).to(group);
                        amqpAdmin.declareBinding(using);
                    } else {
                        Map<String, Object> args = new HashMap<>();
                        args.put("x-dead-letter-exchange", "dead-letter-" + u.getUsername());
                        args.put("x-message-ttl", 360000000);
                        Queue using = new Queue("user." + u.getUsername(), false, false, true, args);
                        Binding userBind = BindingBuilder.bind(using).to(group);
                        amqpAdmin.declareQueue(using);
                        amqpAdmin.declareBinding(userBind);
                    }
                });
            }
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
}
