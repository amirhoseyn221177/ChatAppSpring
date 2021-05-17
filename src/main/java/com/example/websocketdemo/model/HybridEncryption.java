package com.example.websocketdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Data
@AllArgsConstructor
@Component
public class HybridEncryption {
    private final String SymmetricALog  = "AES/CBC/PKCS5Padding";
    private final String AsymmetricALog = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";



    public List<String> encryptingWith_AES_RSA(ChatMessage chatMessage, PublicKey publicKey){

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey AESKey = keyGenerator.generateKey();
            byte[] AESKeyByte = AESKey.getEncoded();
            System.out.println(AESKeyByte.length);

            System.out.println("\n@EncFlow: Original AES Key");
            System.out.println(Arrays.toString(AESKeyByte));
            byte[] messageByte= chatMessage.toString().getBytes(StandardCharsets.UTF_8);
            List<byte[]> encryptedDataAndIv = SymmetricCipherEncryption(messageByte, SymmetricALog, AESKey);
            byte[] encryptedData = encryptedDataAndIv.get(0);
            byte[] ivByte=encryptedDataAndIv.get(1);
            System.out.println("\n@Encrypted Content Bytes");
            System.out.println(Arrays.toString(encryptedData));
            byte[] encryptedAESKey = ASymmetricCipherEncryption(AESKeyByte,AsymmetricALog,publicKey);
            System.out.println("\n@Encrypted Secret Key String: ");
            System.out.println(Arrays.toString(encryptedAESKey));

            return gettingStringOfEncrypted(encryptedData, ivByte, encryptedAESKey);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private List<String> gettingStringOfEncrypted(byte[] encryptedData, byte[] ivByte, byte[] encryptedAESKey) {
        StringBuilder sbData = new StringBuilder();
        StringBuilder sb_AES = new StringBuilder();
        StringBuilder sb_IV = new StringBuilder();

        byte[] base64EncData = Base64.getEncoder().encode(encryptedData);
        byte[] base64AESKey = Base64.getEncoder().encode(encryptedAESKey);
        byte[] base64IV = Base64.getEncoder().encode(ivByte);
        String EncDataString = new String(base64EncData,StandardCharsets.UTF_8);
        String EncAESKey = new String(base64AESKey,StandardCharsets.UTF_8);
        String IV = new String(base64IV,StandardCharsets.UTF_8);

        sbData.append(EncDataString);
        sb_AES.append(EncAESKey);
        sb_IV.append(IV);
        List<String> ContentData = new ArrayList<>();
        ContentData.add(sbData.toString());
        ContentData.add(sb_AES.toString());
        ContentData.add(sb_IV.toString());
        return ContentData;
    }

    public List<byte[]> SymmetricCipherEncryption(byte[] message, String algo, SecretKey secretKey){
        Cipher cipher;
        byte[] encryptedMSG = null;
        List<byte[]> ivAndData=new ArrayList<>();
        try {
            byte [] randomIv = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(randomIv);
            IvParameterSpec iv= new IvParameterSpec(randomIv);
            cipher= Cipher.getInstance(algo);
            cipher.init(Cipher.ENCRYPT_MODE,secretKey,iv);
            encryptedMSG = cipher.doFinal(message);
            ivAndData.add(encryptedMSG);
            ivAndData.add(iv.getIV());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            System.out.println(e.getMessage());

        }

        return ivAndData ;
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
