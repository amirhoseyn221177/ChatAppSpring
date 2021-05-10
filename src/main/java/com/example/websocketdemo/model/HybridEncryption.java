package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Data
@AllArgsConstructor
@Component
public class HybridEncryption {



    public List<String> encryptingWith_AES_RSA(String chatMessage, PublicKey publicKey){

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            SecretKey AESKey = keyGenerator.generateKey();
            byte[] AESKeyByte = AESKey.getEncoded();
            System.out.println("\n@EncFlow: Original AES Key");
            System.out.println(Arrays.toString(AESKeyByte));
            byte[] messageByte= chatMessage.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedData = SymmetricCipherEncryption(messageByte,"AES/ECB/PKCS5Padding",AESKey);
            System.out.println("\n@Encrypted Content Bytes");
            System.out.println(Arrays.toString(encryptedData));
            byte[] encryptedAESKey = ASymmetricCipherEncryption(AESKeyByte,"RSA/ECB/OAEPWithSHA-1AndMGF1Padding",publicKey);
            System.out.println("\n@Encrypted Secret Key String: ");
            System.out.println(Arrays.toString(encryptedAESKey));
            StringBuilder sbData = new StringBuilder();
            StringBuilder sb_AES = new StringBuilder();

            byte[] base64EncData = Base64.getEncoder().encode(encryptedData);
            byte[] base64AESKey = Base64.getEncoder().encode(encryptedAESKey);
            String EncDataString = new String(base64EncData,StandardCharsets.UTF_8);
            String EncAESKey = new String(base64AESKey,StandardCharsets.UTF_8);

            sbData.append(EncDataString);
            sb_AES.append(EncAESKey);
            List<String> ContentData = new ArrayList<>();
            ContentData.add(sbData.toString());
            ContentData.add(sb_AES.toString());

            return ContentData;
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public byte[] SymmetricCipherEncryption(byte[] message, String algo, SecretKey secretKey){
        Cipher cipher;
        byte[] encryptedMSG = null;
        try {
            cipher= Cipher.getInstance(algo);
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            encryptedMSG = cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());

        }

        return encryptedMSG;
    }

    public byte[] ASymmetricCipherEncryption(byte[] message, String algo, PublicKey publicKey){
        Cipher cipher;
        byte[] encryptedMessage=null;

        try {
            cipher=Cipher.getInstance(algo);
            cipher.init(Cipher.ENCRYPT_MODE,publicKey);
            encryptedMessage= cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }
        return encryptedMessage;
    }
}
