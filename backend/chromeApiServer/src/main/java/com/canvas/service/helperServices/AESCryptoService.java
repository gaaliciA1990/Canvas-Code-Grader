package com.canvas.service.helperServices;

import com.canvas.exceptions.CanvasAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * AESCryptoService class that provides methods to encrypt and decrypt Access tokens.
 */
@Service
public class AESCryptoService {
    private static SecretKeySpec secretKey;

    /**
     * Constructor that sets the secret key using the environment property "canvas.secretKey".
     *
     * @param env environment instance
     */
    @Autowired
    public AESCryptoService(Environment env) {
        this.setKey(env.getProperty("canvas.secretKey"));
    }

    private static final Logger logger = LoggerFactory.getLogger(HeaderFilter.class);

    /**
     * Helper method to set the secret key.
     *
     * @param myKey key to be set as secret key
     */
    private void setKey(String myKey) {
        MessageDigest sha = null;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Encrypts the input string using AES algorithm.
     *
     * @param strToEncrypt string to be encrypted
     * @param secret secret to use for encryption
     * @return encrypted string
     * @throws CanvasAPIException if error occurs during encryption
     */
    public String encrypt(String strToEncrypt, String secret) throws CanvasAPIException {
        try {
            //this.setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new CanvasAPIException(e.getMessage());
        }
    }

    /**
     * Decrypts the input string using AES algorithm.
     *
     * @param strToDecrypt string to be decrypted
     * @param secret secret to use for decryption
     * @return decrypted string
     * @throws CanvasAPIException if error occurs during decryption
     */
    public String decrypt(String strToDecrypt, String secret) throws CanvasAPIException {
        try {
            //this.setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new CanvasAPIException(e.getMessage());
        }
    }
}
