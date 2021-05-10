package com.example.websocketdemo.model;


import java.math.BigInteger;
import java.security.*;

import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class RsaKey {


    private final BigInteger mod;
    private final BigInteger prvtInteger;
    private final BigInteger pubInteger;

    public RsaKey(BigInteger mod, BigInteger prvtInteger, BigInteger pubInteger) {
        this.mod = mod;
        this.prvtInteger = prvtInteger;
        this.pubInteger = pubInteger;
    }


    public PrivateKey gettingPrivateKey(){
        try{
            if(prvtInteger==null|| mod==null) throw new InvalidKeyException(" no private expo or Mod ");
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(mod,prvtInteger);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        }catch (Exception e){
            System.out.println("the error is "+ e.getMessage());
        }
        return null;
    }


    public PublicKey gettingPublicKey(){
        try{
            if(pubInteger==null|| mod==null) throw new InvalidKeyException(" no public expo or Mod ");
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod,pubInteger);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return  keyFactory.generatePublic(keySpec);
        }catch (Exception e){
            System.out.println("the error is "+ e.getMessage());

        }
        return null;
    }
}
