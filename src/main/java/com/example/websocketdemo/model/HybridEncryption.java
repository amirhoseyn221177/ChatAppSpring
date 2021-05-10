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
import java.util.Arrays;
import java.util.Base64;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class HybridEncryption {
    private RsaKey rsaKey;


    public String encryptingWith_AES_RSA(String chatMessage){
        BigInteger mod = new BigInteger("00c5963f6bfdbf8c878a1269264e9e905fa7f399acf544ed1f404a25e90ac54640e96a58d440ef9cfba83cc73bdf33ae0045e13a452b6092acc9fa2356e4cdda4eb5df2d9279b734dbde7cc3c9a1e0b1b8c1fa501ea4fe1eda9d45f35bfd2183cf2834d806dc597c83768e4e4e7cbf6d254c28342ac22f00292b4a969989b4c0c26d666dfd26a87f9383238dd9732701c64377f5888fa2e7246edeff4594a48a5687d41360da0d735b0cb73340b3442429efd29665df03252e237e5139e70fbad4c7cb4c94897c065384dce0a0a7f06f3c4dc7ecadc59116a0b6540bae5f2bd0f6e5a8f59e84a95ae7c6a5b28a8f2d9467013cdde3425b86601b2465e63e3c38b9"
                ,16);
        BigInteger pvtExp = new BigInteger(
                "7afbc20e1dab147568e37cbf73400eecfb450cea32dfec17ff9427749e579c1adb2e6e8beebf361f0de086c22573ad624e1c9a80633781143bc38e2b8fcef18afc79258c7e662268985b602228b80ae6cd1658b83a4a523a109a7197583f7c237d0b274b5debf52c7ea6a4e7058ec108ee163822452bac15f3f44138e015b9c432a659f664e0d4c1dd37945ace89f0680a81236212179e045c1601ae635b9cd8136f20a2deae091daf96220e8ed4d7d3ba88e1ad2142eba00dc18ff2e6b14a5085b2625543d218380653c1ccc3d510d51ee11fd07f9934d153cc4f604d81c6e89555f6f34725e009d5afc7b4c8a9ffbc607e825093dd96a505104d737996e061",
                16);
        BigInteger pubExp = new BigInteger("65537");
        RsaKey rsaKey= new RsaKey(mod,pvtExp,pubExp);
        PublicKey publicKey = rsaKey.gettingPublicKey();
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            SecretKey AESKey = keyGenerator.generateKey();
            byte[] AESKeyByte = AESKey.getEncoded();
            System.out.println("\n@EncFlow: Original AES Key");
            System.out.println(Arrays.toString(AESKeyByte));
            byte[] messageByte= chatMessage.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedData = SymmetricCipher(messageByte,"AES/ECB/PKCS5Padding",AESKey);
            System.out.println("\n@Encrypted Content Bytes");
            System.out.println(Arrays.toString(encryptedData));
            byte[] encryptedAESKey = ASymmetricCipher(AESKeyByte,"RSA/ECB/OAEPWithSHA-1AndMGF1Padding",publicKey);
            System.out.println("\n@Encrypted Secret Key String: ");
            System.out.println(Arrays.toString(encryptedAESKey));
            StringBuilder sb = new StringBuilder();

            byte[] base64EncData = Base64.getEncoder().encode(encryptedData);
            byte[] base64AESKey = Base64.getEncoder().encode(encryptedAESKey);
            String EncDataString = new String(base64EncData,StandardCharsets.UTF_8);
            String EncAESKey = new String(base64AESKey,StandardCharsets.UTF_8);
            sb.append(EncDataString).append(System.lineSeparator()).append(EncAESKey);

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public byte[] SymmetricCipher(byte[] message, String algo, SecretKey secretKey){
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

    public byte[] ASymmetricCipher (byte[] message,String algo, PublicKey publicKey){
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
