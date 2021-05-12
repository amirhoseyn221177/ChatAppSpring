package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
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


        byte [] decryptedAESByte = asymmetricCipherDecryption(AES_EncKeyByte,asymmetricAlgo,privateKey);
        System.out.println("\n@Secret Key Bytes: ");
        System.out.println(Arrays.toString(decryptedAESByte));
        SecretKey AES_Key = new SecretKeySpec(decryptedAESByte,0,decryptedAESByte.length,AES_Algo);

        byte[] dataDecrypted = symmetricCipherDecryption(dataEncByte,symmetricAlgo,AES_Key);
        System.out.println("\n@Data Bytes: ");
        System.out.println(Arrays.toString(dataDecrypted));

        System.out.println(new String(dataDecrypted));
        return new String(dataDecrypted);

    }


    public byte[] symmetricCipherDecryption(byte[] encryptedMessage , String algo , SecretKey key){
        Cipher cipher;
        byte [] msg= null;

        try {
            cipher= Cipher.getInstance(algo);
            cipher.init(Cipher.DECRYPT_MODE,key);
            msg=cipher.doFinal(encryptedMessage);

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException |
                IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }

        return  msg;
    }

    public byte[] asymmetricCipherDecryption(byte[] encryptedMsg,String algo,PrivateKey privateKey){
        Cipher cipher;
        byte [] msg= null;
        try {
            cipher= Cipher.getInstance(algo);
            cipher.init(Cipher.DECRYPT_MODE,privateKey);
            msg=cipher.doFinal(encryptedMsg);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException |
                IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e);
        }
        return  msg;
    }

}
