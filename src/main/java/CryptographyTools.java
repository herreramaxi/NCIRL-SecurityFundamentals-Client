/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Maximiliano Herrera
 */
public class CryptographyTools {

    private final String FILE_NAME = "key";
    private final String PADDING_MECHANISM = "AES/CBC/PKCS5Padding";

    public String getHash(String plainText) {
        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            hash = Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }

        return hash;
    }

    public String encrypt(String plainText) {
        try {
            SecretKey key = getStoredKey();
            Cipher cipher = Cipher.getInstance(PADDING_MECHANISM);
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivParams = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);

            byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(bytes);
            String base64String = Base64.getEncoder().encodeToString(encryptedBytes);
            return base64String;
        } catch (Exception e) {
            System.out.println("Error on encrypt: " + e);

            return "";
        }
    }

    public String decrypt(String plainText) {
        if (plainText == null || plainText == "")
            return "";

        try {
            SecretKey key = getStoredKey();
            Cipher cipher = Cipher.getInstance(PADDING_MECHANISM);
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivParams = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);

            byte[] bytes = Base64.getDecoder().decode(plainText);
            byte[] decryptedBytes = cipher.doFinal(bytes);
            String decrypted = new String(decryptedBytes);

            return decrypted;
        } catch (Exception e) {
            System.out.println("Error on decrypt: " + e);

            return "";
        }
    }

    private SecretKey getStoredKey() throws IOException, Exception {
        File f = new File(FILE_NAME);

        if (f.exists() && !f.isDirectory()) {
            FileReader fileReader = new FileReader(FILE_NAME);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String keyAsString = bufferedReader.readLine();
            byte[] decoded = Base64.getDecoder().decode(keyAsString);

            return new SecretKeySpec(decoded, "AES");
        } else {
            System.out.println("key not found, generating and saving a new key...");

            SecretKey generatedKey = generateKey();
            FileWriter fileWriter = new FileWriter(FILE_NAME);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            
            try (PrintWriter pw = new PrintWriter(bufferedWriter)) {
                byte[] encoded = generatedKey.getEncoded();
                String output = Base64.getEncoder().encodeToString(encoded);
                pw.print(output);
                pw.flush();
            }

            return generatedKey;
        }
    }

    private SecretKey generateKey() throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, secureRandom);
        SecretKey generatedKey = keyGenerator.generateKey();

        return generatedKey;
    }
}