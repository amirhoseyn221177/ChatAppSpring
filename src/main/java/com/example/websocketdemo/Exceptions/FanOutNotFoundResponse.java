package com.example.websocketdemo.Exceptions;



public class FanOutNotFoundResponse {
    private String message;


    public FanOutNotFoundResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
