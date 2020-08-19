package com.example.websocketdemo.Exceptions;

public class GroupNotFoundResponse {
    private String message;

    public GroupNotFoundResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
