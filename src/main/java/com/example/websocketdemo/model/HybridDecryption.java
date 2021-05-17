package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Data
@NoArgsConstructor
@Component
public class HybridDecryption {


    public String decryptionFlow(List<String> encryptedMessage, PrivateKey privateKey, String AES_Algo, String symmetricAlgo, String asymmetricAlgo){
        byte [] dataEncByte = Base64.getDecoder().decode(encryptedMessage.get(0));
        byte [] AES_EncKeyByte = Base64.getDecoder().decode(encryptedMessage.get(1));
        byte [] IVByte = Base64.getDecoder().decode(encryptedMessage.get(2));


        byte [] decryptedAESByte = asymmetricCipherDecryption(AES_EncKeyByte,asymmetricAlgo,privateKey);
        System.out.println("\n@Secret Key Bytes: ");
        System.out.println(Arrays.toString(decryptedAESByte));
        SecretKey AES_Key = new SecretKeySpec(decryptedAESByte,0,decryptedAESByte.length,AES_Algo);

        byte[] dataDecrypted = symmetricCipherDecryption(dataEncByte,symmetricAlgo,AES_Key.getEncoded(),IVByte);
        System.out.println("\n@Data Bytes: ");
        System.out.println(Arrays.toString(dataDecrypted));

        System.out.println(new String(dataDecrypted));
        return new String(dataDecrypted);

    }


    public byte[] symmetricCipherDecryption(byte[] encryptedMessage , String algo , byte[] key, byte[] ivByte){
        Cipher cipher;
        byte [] msg= null;
        IvParameterSpec iv = new IvParameterSpec(ivByte);
        SecretKey secretKey= new SecretKeySpec(key,0,key.length,"AES");
        try {
            cipher= Cipher.getInstance(algo);
            cipher.init(Cipher.DECRYPT_MODE,secretKey,iv);
            msg=cipher.doFinal(encryptedMessage);

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException |
                BadPaddingException | InvalidAlgorithmParameterException e) {
            System.out.println(e.getMessage());
        }

        return  msg;
    }

    public byte[] asymmetricCipherDecryption(byte[] encryptedMsg,String algo,PrivateKey privateKey){
        Cipher cipher;
        byte [] msg= null;
        try {
            cipher= Cipher.getInstance(algo);
            System.out.println(65);
            System.out.println(privateKey);
            cipher.init(Cipher.DECRYPT_MODE,privateKey);
            msg=cipher.doFinal(encryptedMsg);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException |
                IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }
        return  msg;
    }

}
