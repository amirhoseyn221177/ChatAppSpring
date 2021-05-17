package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.PublicKey;
import java.util.*;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class ChatUser implements UserDetails {
    @Id
    private String id;
    @NotBlank(message = "you should have a name ")
    private String name;
    @NotBlank(message = "each user has to have a username ")
    private String username;
    private List<String> groupChats = new ArrayList<>();
    private List<String> friends = new ArrayList<>();
    @NotBlank(message = "password should not be blank")
    @Size(min = 4,max = 10, message = "please use 4 to 10 characters")
    private String password;
    private List<Role> roles=new ArrayList<>();
    private Map<String,UserPublicKey> keyManageUsers = new HashMap<>();
    private PublicKey publicKey;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
