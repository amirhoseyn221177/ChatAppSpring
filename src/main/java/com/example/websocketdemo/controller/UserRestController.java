package com.example.websocketdemo.controller;

import com.example.websocketdemo.Exceptions.MapValidationError;
import com.example.websocketdemo.Services.PrivateChatServices;
import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.HybridDecryption;
import com.example.websocketdemo.model.LoginRequest;
import com.example.websocketdemo.model.testClass;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/user")
@CrossOrigin("*")

public class UserRestController {

    private final MapValidationError mapValidationError;
    private final PrivateChatServices privateChatServices;
    private final HybridDecryption hybridDecryption;


    public UserRestController(MapValidationError mapValidationError, PrivateChatServices privateChatServices, HybridDecryption hybridDecryption) {
        this.mapValidationError = mapValidationError;
        this.privateChatServices = privateChatServices;

        this.hybridDecryption = hybridDecryption;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registering(@RequestBody ChatUser chatUser, BindingResult result) {
        try {
            System.out.println(chatUser);
            ResponseEntity<?> error = mapValidationError.MapValidationService(result);
            if (error != null) return error;
            ChatUser chatUser1 = privateChatServices.createUser(chatUser);
            return new ResponseEntity<>(chatUser1, HttpStatus.ACCEPTED);
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
            return new ResponseEntity<>("this user name is taken", HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, BindingResult result,
                                   @RequestHeader Map<String, Object> allHeaders) {

        ResponseEntity<?> error = mapValidationError.MapValidationService(result);
        if (error != null) return error;
        String jwt = privateChatServices.SendToken(loginRequest.getUsername(), loginRequest.getPassword());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", jwt);
        Map<String, Object> body = new HashMap<>();
        body.put("token", jwt);
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/dec")
    public ResponseEntity<?> gettingData(@RequestBody testClass objects){
        System.out.println("we are here");
        System.out.println(objects.getKey());
        byte[] keys = Base64.getDecoder().decode(objects.getKey());
        byte[] iv = Base64.getDecoder().decode(objects.getIv());
        System.out.println(Arrays.toString(keys));
        byte[] text = Base64.getDecoder().decode(objects.getText());
        System.out.println(text.length);
        System.out.println(new String(hybridDecryption.symmetricCipherDecryption(text,"AES/CBC/PKCS5Padding",keys,iv)));
        return  new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/rsa/{username}")
    public  ResponseEntity<?> sendingRSA( @PathVariable String username , BindingResult result){
        try{
            System.out.println(username);
            ResponseEntity<?> error = mapValidationError.MapValidationService(result);
            if(error!= null) return  error;
            byte[] RSAPublic = privateChatServices.userRSAPublicKey(username);
            return  new ResponseEntity<>(RSAPublic, HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
            return  new ResponseEntity<>("there is no such a user",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/exist")
    public void checkIfUserExist(@RequestBody String phoneNumber){
        System.out.println(phoneNumber);
    }

    @GetMapping("/rsa/provide/{username}")
    public  ResponseEntity<?> askingForRSA(@PathVariable String username,@RequestBody byte[] rsa,BindingResult result){
        try{
            ResponseEntity<?> error = mapValidationError.MapValidationService(result);
            if(error!=null)return error;
            privateChatServices.saveRSA(rsa,username);
            return new ResponseEntity<>("saved new RSA", HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
            return  new ResponseEntity<>("could not get RSA",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/startchat/{username}/{friend}")
    public ResponseEntity<?> addToFriendList(@PathVariable String username, @PathVariable String friend ){
        byte[] friendRSA = privateChatServices.newFriend(username,friend);
        Map<String, Object> body = new HashMap<>();
        body.put("friendRSA", friendRSA);
        return  new ResponseEntity<>(body,HttpStatus.OK);
    }
}
