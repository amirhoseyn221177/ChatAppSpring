package com.example.websocketdemo.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class userNotFoundException extends RuntimeException{
    public userNotFoundException(String message) {
        super(message);
    }
}
