package com.example.websocketdemo.Services;

import com.example.websocketdemo.Repository.UserRepo;
import com.example.websocketdemo.model.ChatUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.util.Optional;

@Service
public class CustomUserServices implements UserDetailsService {
    private final UserRepo userRepo;

    public CustomUserServices(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ChatUser> chatUser=userRepo.findByUsername(username);
        return chatUser.orElse(null);
    }

    @Transient
    public ChatUser loadByID(String Id){
        Optional<ChatUser> chatUser =userRepo.findById(Id);
        return chatUser.orElse(null);
    }
}
