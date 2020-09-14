package com.example.websocketdemo.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.attribute.UserPrincipalNotFoundException;

@ControllerAdvice
@RestController
public class CustomException extends ResponseEntityExceptionHandler {


    @ExceptionHandler
    public final ResponseEntity<Object> handleNoGroup(GroupNotFoundException ex, WebRequest webRequest){
        GroupNotFoundResponse groupNotFoundResponse = new GroupNotFoundResponse(ex.getMessage());
        return new ResponseEntity<>(groupNotFoundResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleNoUser(UserPrincipalNotFoundException ex,WebRequest webRequest){
        UserNotFoundResponse userNotFoundResponse = new UserNotFoundResponse(ex.getMessage());
        return new ResponseEntity<>(userNotFoundResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleNoExchange(FanOutNotFoundException ex,WebRequest webRequest){
        FanOutNotFoundResponse fanOutNotFoundResponse= new FanOutNotFoundResponse(ex.getMessage());
        return new ResponseEntity<>(fanOutNotFoundResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleUSerExist(UsernameAlreadyExistException ex, WebRequest webRequest){
        UsernameAlreadyExistResponse usernameAlreadyExistResponse=new UsernameAlreadyExistResponse(ex.getMessage());
        return new ResponseEntity<>(usernameAlreadyExistResponse,HttpStatus.BAD_REQUEST);
    }

}
