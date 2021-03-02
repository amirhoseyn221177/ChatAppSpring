package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RabbitTools {
    private boolean durable=false;
    private boolean exclusive=false;
    private boolean auto_delete=true;
    private String x_dead_letter_exchange="dead+letter";
    private int x_message_ttl=360000000;

}
