package com.example.websocketdemo.Exceptions;

public class BadTokenResponse {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BadTokenResponse(String message) {
        this.message = message;
    }
}
