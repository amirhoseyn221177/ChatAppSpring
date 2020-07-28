package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.stream.Stream;


@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupChat {
    @Id
    private String Id;
    private String name;
    private List<ChatUser> members;

}
