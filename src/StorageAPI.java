import Interfaces.StorageImplementation;
import Interfaces.Passwords;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.UUID;

public class StorageAPI implements StorageImplementation {
    private static final String FILE_PATH = "passwords.csv"; // File to store passwords
    private static final String SALT_FILE_PATH = "salt.bin"; // File to store the salt
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final String MASTER_PASSWORD_FILE = "master_password.bin"; // File to store the hashed master password

    private LinkedHashMap<String, Passwords> storage = new LinkedHashMap<>();
    private final String masterPassword;

    public StorageAPI(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    @Override
    public String getStorage(String storageId) {
        return storage.get(storageId) != null ? storage.get(storageId).getPassword() : null;
    }

    @Override
    public String setStorage(Passwords password) {
        String id = UUID.randomUUID().toString();
        storage.put(id, password);
        saveToFile();
        return id;
    }

    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return storage;
    }

    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (var entry : storage.entrySet()) {
                String id = entry.getKey();
                Passwords password = entry.getValue();
                String line = id + "," + password.getName() + "," + password.getUsername() + "," + password.getPassword();
                String encryptedLine = encrypt(line, masterPassword);
                writer.write(encryptedLine);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            // If the file does not exist, simply return without throwing an exception
            System.out.println("No existing password file found. Starting with an empty storage.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String decryptedLine = decrypt(line, masterPassword);
                String[] parts = decryptedLine.split(",");
                if (parts.length == 4) {
                    String id = parts[0];
                    String name = parts[1];
                    String username = parts[2];
                    String passwordValue = parts[3];

                    Standart password = new Standart();
                    password.setName(name);
                    password.setUsername(username);
                    password.setPassword(passwordValue);

                    storage.put(id, password);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading passwords from file: " + e.getMessage());
        }
    }

    private String encrypt(String data, String password) throws IOException {
        try {
            SecretKey key = generateKey(password);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new IOException("Error encrypting data", e);
        }
    }

    private String decrypt(String data, String password) throws IOException {
        try {
            SecretKey key = generateKey(password);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(data);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new IOException("Error decrypting data", e);
        }
    }

    private SecretKey generateKey(String password) throws Exception {
        byte[] salt = loadOrGenerateSalt();
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
    }

    private byte[] loadOrGenerateSalt() throws IOException {
        File saltFile = new File(SALT_FILE_PATH);

        // Check if the salt file exists
        if (saltFile.exists()) {
            try (FileInputStream fis = new FileInputStream(saltFile)) {
                byte[] salt = new byte[SALT_LENGTH];
                if (fis.read(salt) != SALT_LENGTH) {
                    throw new IOException("Invalid salt file");
                }
                System.out.println("Salt file loaded successfully.");
                return salt;
            } catch (IOException e) {
                System.out.println("Error reading salt file. Regenerating salt...");
            }
        } else {
            System.out.println("Salt file not found. Generating a new salt...");
        }

        // Generate a new salt
        byte[] salt = new byte[SALT_LENGTH];
        try {
            SecureRandom.getInstanceStrong().nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Strong secure random algorithm not available. Falling back to default.");
            try {
                SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
            } catch (NoSuchAlgorithmException fallbackException) {
                throw new IOException("No secure random algorithm available", fallbackException);
            }
        }

        // Save the generated salt to the file
        try (FileOutputStream fos = new FileOutputStream(saltFile)) {
            fos.write(salt);
            System.out.println("New salt file created.");
        }
        return salt;
    }

    // Method to set the master password
    public void setMasterPassword(String password) throws IOException {
        byte[] hashedPassword = hashPassword(password);
        try (FileOutputStream fos = new FileOutputStream(MASTER_PASSWORD_FILE)) {
            fos.write(hashedPassword);
        }
    }

    // Method to verify the master password
    public boolean verifyMasterPassword(String password) throws IOException {
        File file = new File(MASTER_PASSWORD_FILE);
        if (!file.exists()) {
            throw new IOException("Master password not set. Please set it first.");
        }

        byte[] storedHashedPassword;
        try (FileInputStream fis = new FileInputStream(file)) {
            storedHashedPassword = fis.readAllBytes();
        }

        byte[] hashedPassword = hashPassword(password);
        return Arrays.equals(storedHashedPassword, hashedPassword);
    }

    // Hash the password using SHA-256 (or use a more secure algorithm like Argon2 or bcrypt)
    private byte[] hashPassword(String password) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(password.getBytes());
        } catch (Exception e) {
            throw new IOException("Error hashing password", e);
        }
    }
}
