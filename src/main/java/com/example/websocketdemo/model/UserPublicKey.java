package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Date;

@Data
@AllArgsConstructor
public class UserPublicKey {
    private final PublicKey publicKey;
    private final Date createdPublicKeyTime;
}
