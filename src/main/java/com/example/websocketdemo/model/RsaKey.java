package com.example.websocketdemo.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;
import java.security.*;


@Component
@Data
@AllArgsConstructor
public class RsaKey {
    private  PrivateKey privateKey;
    private  PublicKey publicKey;

    public RsaKey() {
        this.gettingKeyPair();
    }

    public void gettingKeyPair(){
        // this method uses user defined exponents to create a private key
//        try{
//            if(prvtInteger==null|| mod==null) throw new InvalidKeyException(" no private expo or Mod ");
//            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(mod,prvtInteger);
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            return keyFactory.generatePrivate(keySpec);
//
//        }catch (Exception e){
//            System.out.println("the error is "+ e.getMessage());
//        }

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom secureRandom = new SecureRandom();
            keyPairGenerator.initialize(1028,secureRandom);
            KeyPair keyPair= keyPairGenerator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }


}
