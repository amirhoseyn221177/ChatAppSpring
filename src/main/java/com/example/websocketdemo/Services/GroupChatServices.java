package com.example.websocketdemo.Services;

import com.example.websocketdemo.Exceptions.GroupNotFoundException;
import com.example.websocketdemo.Exceptions.userNotFoundException;
import com.example.websocketdemo.Repository.GroupChatRepo;
import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.GroupChat;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GroupChatServices {
    private final RabbitTemplate rabbitTemplate;
    private final UserRepo userRepo;
    private final GroupChatRepo groupChatRepo;
    private final AmqpAdmin amqpAdmin;
    private final RabbitManagementTemplate rabbitManagementTemplate;
    public GroupChatServices(RabbitTemplate rabbitTemplate, UserRepo userRepo, GroupChatRepo groupChatRepo, AmqpAdmin amqpAdmin, RabbitManagementTemplate rabbitManagementTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.userRepo = userRepo;
        this.groupChatRepo = groupChatRepo;
        this.amqpAdmin = amqpAdmin;
        this.rabbitManagementTemplate = rabbitManagementTemplate;
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
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        AtomicReference<String> name = new AtomicReference<>("");
        groupChat.ifPresent(groupChat1 -> {
            Optional<ChatUser> chatUser = userRepo.findByUsername(chatMessage.getSender());
            if (chatUser.isPresent()) {
                if (chatUser.get().getGroupChats().contains(groupId)) {
                    System.out.println("user in groupChat");
                    name.set(groupChat1.getName());
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

    public List<GroupChat> getAllGroups() {
        return groupChatRepo.findAll();
    }

    public GroupChat getGroup(String groupId) {
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        return groupChat.orElse(null);
    }

    public void createQueuesAndExchangeForGroupChat(String groupId, String sender) {
        Optional<GroupChat> groupChat = groupChatRepo.findById(groupId);
        groupChat.ifPresent(groupChat1 -> {
            String exchange = "G_" + groupChat1.getName();
            FanoutExchange group = new FanoutExchange(exchange, false, true, null);
            List<String> chatUsers = groupChat1.getMembers();
            amqpAdmin.declareExchange(group);
            for (String id : chatUsers) {
                Optional<ChatUser> user = userRepo.findById(id);
                user.ifPresent(u -> {
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
        });
        if (groupChat.isEmpty()) {
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


}
