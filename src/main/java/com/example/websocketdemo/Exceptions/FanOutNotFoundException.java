package com.example.websocketdemo.Exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FanOutNotFoundException extends RuntimeException {
    public FanOutNotFoundException(String message) {
        super(message);
    }
}
