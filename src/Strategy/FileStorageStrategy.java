package Strategy;

import Composite.Category;
import Interfaces.PasswordCategory;
import Interfaces.Passwords;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileStorageStrategy extends AbstractStorageStrategy {
    private final String masterPassword;
    private final String passwordsFilePath;
    private final String categoriesFilePath;
    private SecretKey secretKey;
    
    public FileStorageStrategy(String masterPassword) throws IOException {
        super();
        this.masterPassword = masterPassword;
        this.passwordsFilePath = "passwords.bin";
        this.categoriesFilePath = "categories.bin";
        
        try {
            initializeEncryption();
            loadFromFiles();
        } catch (Exception e) {
            System.err.println("Error initializing file storage: " + e.getMessage());
            // Initialize with empty state
            this.passwordCache = new LinkedHashMap<>();
            this.rootCategory = new Category("Root");
            
            // Rethrow as IOException if it's a file access issue
            if (e instanceof IOException) {
                throw (IOException)e;
            }
        }
    }
    
    private void initializeEncryption() throws Exception {
        // Generate a secret key from master password
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(masterPassword.getBytes(StandardCharsets.UTF_8));
        this.secretKey = new SecretKeySpec(keyBytes, 0, 16, "AES");
    }
    
    @Override
    public String getPassword(String id) {
        Passwords password = passwordCache.get(id);
        return password != null ? password.getPassword() : null;
    }
    
    @Override
    public String savePassword(Passwords password) {
        String id = UUID.randomUUID().toString();
        passwordCache.put(id, password);
        saveState(); // Automatically save changes to file
        return id;
    }
    
    @Override
    public void addPasswordToCategory(String categoryPath, Passwords password) {
        // Implement category path handling
        String[] pathComponents = categoryPath.split("/");
        Category current = (Category) rootCategory; // Cast to Category since rootCategory is of type Category
        
        // Navigate/create category path
        for (String component : pathComponents) {
            if (component.isEmpty()) continue;
            
            Category found = null;
            for (PasswordCategory child : current.getChildren()) {
                if (child instanceof Category && ((Category) child).getName().equals(component)) {
                    found = (Category) child;
                    break;
                }
            }
            
            if (found == null) {
                found = new Category(component);
                current.add(found);
            }
            
            current = found;
        }
        
        // Add password to final category
        String id = savePassword(password);
        current.addPasswordId(id);
        
        saveState();
    }
    
    @Override
    public void saveState() {
        try {
            // Save passwords
            String serializedPasswords = serializePasswords();
            String encryptedPasswords = encrypt(serializedPasswords);
            writeToFile(passwordsFilePath, encryptedPasswords);
            
            // Save categories
            String serializedCategories = serializeCategories();
            String encryptedCategories = encrypt(serializedCategories);
            writeToFile(categoriesFilePath, encryptedCategories);
            
        } catch (Exception e) {
            System.err.println("Error saving state: " + e.getMessage());
        }
    }
    
    private void loadFromFiles() {
        try {
            // Load passwords
            if (fileExists(passwordsFilePath)) {
                String encryptedPasswords = readFromFile(passwordsFilePath);
                String decryptedPasswords = decrypt(encryptedPasswords);
                deserializePasswords(decryptedPasswords);
            }
            
            // Load categories
            if (fileExists(categoriesFilePath)) {
                String encryptedCategories = readFromFile(categoriesFilePath);
                String decryptedCategories = decrypt(encryptedCategories);
                deserializeCategories(decryptedCategories);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading state: " + e.getMessage());
            // Initialize with empty state
            this.passwordCache = new LinkedHashMap<>();
            this.rootCategory = new Category("Root");
        }
    }
    
    // File operations
    private void writeToFile(String filePath, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    private String readFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];
        
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        }
        
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    private boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
    
    // Encryption/decryption
    private String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    private String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    
    // Serialization methods
    private String serializePasswords() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Passwords> entry : passwordCache.entrySet()) {
            Passwords pwd = entry.getValue();
            sb.append(entry.getKey()).append(":")
              .append(pwd.getName()).append(":")
              .append(pwd.getUsername()).append(":")
              .append(pwd.getPassword()).append(":")
              .append(pwd.getType()).append("\n");
        }
        return sb.toString();
    }
    
    private void deserializePasswords(String serialized) {
        String[] lines = serialized.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(":");
            if (parts.length == 5) {
                String id = parts[0];
                String name = parts[1];
                String username = parts[2];
                String passwordValue = parts[3];
                Interfaces.PasswordType type = Interfaces.PasswordType.valueOf(parts[4]);
                
                try {
                    Passwords password = Factory.FactoryPassword.makePassword(type);
                    password.setName(name);
                    password.setUsername(username);
                    password.setPassword(passwordValue);
                    passwordCache.put(id, password);
                } catch (Exceptions.UndefinedPasswordException e) {
                    System.err.println("Error deserializing password: " + e.getMessage());
                }
            }
        }
    }
    
    private String serializeCategories() {
    // Basic implementation for demonstration
    StringBuilder sb = new StringBuilder();
    serializeCategoryRecursive(rootCategory, "", sb);
    return sb.toString();
}

private void serializeCategoryRecursive(Category category, String path, StringBuilder sb) {
    String currentPath = path.isEmpty() ? category.getName() : path + "/" + category.getName();
    
    // Save category and its password IDs
    if (!category.getPasswordIds().isEmpty()) {
        sb.append(currentPath).append(":").append(String.join(",", category.getPasswordIds())).append("\n");
    } else {
        sb.append(currentPath).append(":\n"); // Empty category
    }
    
    // Process children
    for (PasswordCategory child : category.getChildren()) {
        if (child instanceof Category) {
            serializeCategoryRecursive((Category) child, currentPath, sb);
        }
    }
}

private void deserializeCategories(String serialized) {
    // Reset root category
    rootCategory = new Category("Root");
    
    // Parse serialized categories
    String[] lines = serialized.split("\n");
    for (String line : lines) {
        if (line.trim().isEmpty()) continue;
        
        String[] parts = line.split(":");
        if (parts.length == 2) {
            String categoryPath = parts[0];
            String passwordIdsString = parts[1];
            
            // Create category path
            String[] pathComponents = categoryPath.split("/");
            Category current = rootCategory;
            
            // Skip root category if it's the first component
            int startIdx = 0;
            if (pathComponents.length > 0 && pathComponents[0].equals("Root")) {
                startIdx = 1;
            }
            
            // Create category path
            for (int i = startIdx; i < pathComponents.length; i++) {
                String component = pathComponents[i];
                if (component.isEmpty()) continue;
                
                Category found = null;
                for (PasswordCategory child : current.getChildren()) {
                    if (child instanceof Category && ((Category) child).getName().equals(component)) {
                        found = (Category) child;
                        break;
                    }
                }
                
                if (found == null) {
                    found = new Category(component);
                    current.add(found);
                }
                
                current = found;
            }
            
            // Add password IDs to the category
            if (!passwordIdsString.isEmpty()) {
                String[] passwordIds = passwordIdsString.split(",");
                for (String id : passwordIds) {
                    if (!id.isEmpty()) {
                        current.addPasswordId(id);
                    }
                }
            }
        }
    }
}
}