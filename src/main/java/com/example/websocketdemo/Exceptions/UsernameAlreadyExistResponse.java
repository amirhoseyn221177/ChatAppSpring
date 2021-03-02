package com.example.websocketdemo.Exceptions;

public class UsernameAlreadyExistResponse {
    private String message;

    public UsernameAlreadyExistResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
