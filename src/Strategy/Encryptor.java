package Strategy;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Encryptor {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; 
    private static final int KEY_LENGTH = 256;
    private static final String SALT_FILE_PATH = "salt.bin";
    private static final String IV_FILE_PATH = "iv.bin";
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    
    private final SecretKey secretKey;
    private final IvParameterSpec iv;

    public Encryptor(String masterPassword) {
        this.secretKey = generateKey(masterPassword);
        this.iv = getIV();
    }

    public String encrypt(String data) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new IOException("Error encrypting data: " + e.getMessage(), e);
        }
    }

    public String decrypt(String encryptedData) throws IOException {
        try {
            encryptedData = encryptedData.trim();
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (IllegalArgumentException e) {
            throw new IOException("Base64 decoding error - data may be corrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Error decrypting data: " + e.getMessage(), e);
        }
    }

    private SecretKey generateKey(String password) {
        try {
            byte[] salt = getSalt();
            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                65536,
                KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Error generating encryption key: " + e.getMessage(), e);
        }
    }

    private byte[] getSalt() {
        File saltFile = new File(SALT_FILE_PATH);
        
        if (saltFile.exists()) {
            try (FileInputStream fis = new FileInputStream(saltFile)) {
                byte[] salt = new byte[SALT_LENGTH];
                int bytesRead = fis.read(salt);
                
                if (bytesRead == SALT_LENGTH) {
                    System.out.println("Loaded existing salt from file");
                    return salt;
                } else {
                    System.out.println("Salt file exists but is invalid, regenerating");
                }
            } catch (IOException e) {
                System.err.println("Error reading salt file: " + e.getMessage());
            }
        }
        
        byte[] salt = new byte[SALT_LENGTH];
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            secureRandom.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Strong secure random not available, using default");
            new SecureRandom().nextBytes(salt);
        }
        
        try (FileOutputStream fos = new FileOutputStream(saltFile)) {
            fos.write(salt);
            System.out.println("Generated and saved new salt");
        } catch (IOException e) {
            System.err.println("Warning: Could not save salt to file: " + e.getMessage());
        }
        
        return salt;
    }
    
    private IvParameterSpec getIV() {
        File ivFile = new File(IV_FILE_PATH);
        byte[] iv = new byte[IV_LENGTH];
        
        if (ivFile.exists()) {
            try (FileInputStream fis = new FileInputStream(ivFile)) {
                int bytesRead = fis.read(iv);
                if (bytesRead == IV_LENGTH) {
                    System.out.println("Loaded existing IV from file");
                    return new IvParameterSpec(iv);
                }
            } catch (IOException e) {
                System.err.println("Error reading IV file: " + e.getMessage());
            }
        }
        
        try {
            SecureRandom.getInstanceStrong().nextBytes(iv);
        } catch (NoSuchAlgorithmException e) {
            new SecureRandom().nextBytes(iv);
        }
        
        try (FileOutputStream fos = new FileOutputStream(ivFile)) {
            fos.write(iv);
            System.out.println("Generated and saved new IV");
        } catch (IOException e) {
            System.err.println("Warning: Could not save IV to file: " + e.getMessage());
        }
        
        return new IvParameterSpec(iv);
    }
}