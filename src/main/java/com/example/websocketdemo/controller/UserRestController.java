package com.example.websocketdemo.controller;

import com.amazonaws.services.xray.model.Http;
import com.amazonaws.util.IOUtils;
import com.example.websocketdemo.Exceptions.MapValidationError;
import com.example.websocketdemo.Services.ChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.LoginRequest;
import com.google.gson.Gson;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin("*")

public class UserRestController {

    private final MapValidationError mapValidationError;
    private final ChatServices chatServices;


    public UserRestController(MapValidationError mapValidationError, ChatServices chatServices) {
        this.mapValidationError = mapValidationError;
        this.chatServices = chatServices;

    }

    @PostMapping("/register")
    public ResponseEntity<?> registering(@RequestBody ChatUser chatUser, BindingResult result) {
        try {
            System.out.println(chatUser);
            ResponseEntity<?> error = mapValidationError.MapValidationService(result);
            if (error != null) return error;
            ChatUser chatUser1 = chatServices.createUser(chatUser);
            return new ResponseEntity<>(chatUser1, HttpStatus.ACCEPTED);
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
            return new ResponseEntity<>("this user name is taken", HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, BindingResult result,
                                   @RequestHeader Map<String, Object> allHeaders) {
        System.out.println(allHeaders);
        System.out.println(loginRequest);
        System.out.println(51);
        ResponseEntity<?> error = mapValidationError.MapValidationService(result);
        if (error != null) return error;
        String jwt = chatServices.SendToken(loginRequest.getUsername(), loginRequest.getPassword());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", jwt);
        Map<String, Object> body = new HashMap<>();
        body.put("token", jwt);
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadingFile(){
        ByteArrayResource inputStreamResource= chatServices.gettingFile();

        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add("content-Type","application/octet-stream");
//        httpHeaders.add("content");

        try {
            return ResponseEntity.ok()
                    .contentLength(inputStreamResource.contentLength())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(inputStreamResource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
