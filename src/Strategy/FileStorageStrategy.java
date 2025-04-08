package Strategy;

import Composite.Category;
import Interfaces.PasswordCategory;
import Interfaces.Passwords;
import Interfaces.PasswordType;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import Factory.FactoryPassword;

public class FileStorageStrategy extends AbstractStorageStrategy {
    private final String passwordsFilePath;
    private final String categoriesFilePath;
    private Encryptor encryptor;
    
    // Add a flag to track if data has been loaded
    private boolean dataLoaded = false;
    
    public FileStorageStrategy(String masterPassword) throws IOException {
        this(masterPassword, true);
    }
    
    public FileStorageStrategy(String masterPassword, boolean initializeState) throws IOException {
        super();
        this.passwordsFilePath = "passwords.bin";
        this.categoriesFilePath = "categories.bin";
        
        try {
            this.encryptor = new Encryptor(masterPassword);
            
            // Only load files if master password is valid and we want to initialize
            if (masterPassword != null && !masterPassword.isEmpty() && initializeState) {
                loadFromFiles();
                dataLoaded = true;
            }
        } catch (Exception e) {
            System.err.println("Error initializing file storage: " + e.getMessage());
            this.passwordCache = new LinkedHashMap<>();
            this.rootCategory = new Category("Root");
        }
    }
    
    // Add method to explicitly load data after master password is set
    public void loadDataWithMasterPassword(String masterPassword) {
        if (dataLoaded) return; // Don't reload if already loaded
        
        try {
            this.encryptor = new Encryptor(masterPassword);
            System.out.println("Attempting to load data with provided master password...");
            loadFromFiles();
            dataLoaded = true;
            System.out.println("Data loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading with master password: " + e.getMessage());
            e.printStackTrace(); // Add stack trace for debugging
        }
    }

    public String getPassword(String id) {
        Passwords password = passwordCache.get(id);
        return password != null ? password.getPassword() : null;
    }

    public String savePassword(Passwords password) {
        String id = UUID.randomUUID().toString();
        passwordCache.put(id, password);
        saveState(); // Automatically save changes to file
        return id;
    }
    
    public void addPasswordToCategory(String categoryPath, Passwords password) {
        // Split path into components, trimming any trailing slashes
        String trimmedPath = categoryPath.replaceAll("/+$", "");
        String[] pathComponents = trimmedPath.split("/");
        
        Category current = rootCategory;
        for (String component : pathComponents) {
            // Skip empty path components
            if (component.trim().isEmpty()) continue;
            
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
        
        // Add password to final category if provided
        if (password != null) {
            // Check if this password is already in the cache
            String existingId = null;
            for (Map.Entry<String, Passwords> entry : passwordCache.entrySet()) {
                if (entry.getValue() == password) {
                    existingId = entry.getKey();
                    break;
                }
            }
            
            // If not found, save it
            String id = existingId != null ? existingId : savePassword(password);
            current.addPasswordId(id);
        }
        
        saveState();
    }
    
    @Override
    public void saveState() {
        try {
            // Don't save empty password cache if we had loaded passwords before
            if (dataLoaded && passwordCache.isEmpty()) {
                System.out.println("Warning: Preventing save of empty password cache that would overwrite existing data.");
                return;
            }
            cleanupNullPasswords();
            cleanupCategoryReferences();
            
            // Save passwords
            String serializedPasswords = serializePasswords();
            String encryptedPasswords = encryptor.encrypt(serializedPasswords);
            writeToFile(passwordsFilePath, encryptedPasswords);
            
            // Save categories
            String serializedCategories = serializeCategories();
            String encryptedCategories = encryptor.encrypt(serializedCategories);
            writeToFile(categoriesFilePath, encryptedCategories);
            
            System.out.println("State saved successfully.");
            
        } catch (Exception e) {
            System.err.println("Error saving state: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadFromFiles() {
        try {
            // Load passwords 
            if (fileExists(passwordsFilePath)) {
                try {
                    String encryptedContent = readFromFile(passwordsFilePath);
                    String serializedPasswords = encryptor.decrypt(encryptedContent);
                    deserializePasswords(serializedPasswords);
                    System.out.println("Successfully loaded " + passwordCache.size() + " passwords.");
                } catch (Exception e) {
                    // Combined catch block 
                    if (e instanceof javax.crypto.BadPaddingException) {
                        System.err.println("Decryption error - incorrect master password or corrupted file: " + e.getMessage());
                    } else {
                        System.err.println("Error loading passwords: " + e.getMessage());
                    }
                    throw e; // Rethrow so the parent method knows there was an error
                }
            }
            
            
            // Load categories
            if (fileExists(categoriesFilePath)) {
                try {
                    String encryptedContent = readFromFile(categoriesFilePath);
                    String serializedCategories = encryptor.decrypt(encryptedContent);
                    deserializeCategories(serializedCategories);
                    System.out.println("Successfully loaded category structure.");
                } catch (Exception e) {
                    // Combined catch block 
                    if (e instanceof javax.crypto.BadPaddingException) {
                        System.err.println("Decryption error - incorrect master password or corrupted file: " + e.getMessage());
                    } else {
                        System.err.println("Error loading categories: " + e.getMessage());
                    }
                    throw e; // Rethrow so the parent method knows there was an error
                }
            }
            
            // Clean up any null password entries
            cleanupNullPasswords();
            
        } catch (Exception e) {
            System.err.println("Error loading state from files: " + e.getMessage());
            throw new RuntimeException("Failed to load encrypted data", e);
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
    
    // Serialization methods
    private String serializePasswords() {
        StringBuilder sb = new StringBuilder();
        System.out.println("Serializing " + passwordCache.size() + " passwords.");
        
        for (Map.Entry<String, Passwords> entry : passwordCache.entrySet()) {
            String id = entry.getKey();
            Passwords pwd = entry.getValue();
            
            if (pwd == null) {
                System.err.println("Warning: Null password found with ID: " + id);
                continue;
            }
            
            // Now safe to call methods on pwd
            sb.append(id).append(":")
              .append(pwd.getName() != null ? pwd.getName() : "").append(":")
              .append(pwd.getUsername() != null ? pwd.getUsername() : "").append(":")
              .append(pwd.getPassword() != null ? pwd.getPassword() : "").append(":")
              .append(pwd.getType()).append("\n");
        }
        return sb.toString();
    }

    private void deserializePasswords(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) {
            System.out.println("No password data to deserialize.");
            return;
        }
        
        passwordCache.clear();
        String[] lines = serialized.split("\n");
        System.out.println("Deserializing " + lines.length + " password entries.");
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(":", 5); // Limit to 5 parts
            if (parts.length < 5) {
                System.err.println("Invalid password entry: " + line);
                continue;
            }
            
            String id = parts[0];
            String name = parts[1];
            String username = parts[2];
            String password = parts[3];
            String type = parts[4];
            
            try {
                // Convert string type to PasswordType enum
                PasswordType passwordType = PasswordType.valueOf(type);
                Passwords pwd = FactoryPassword.makePassword(passwordType);
                pwd.setName(name);
                pwd.setUsername(username);
                pwd.setPassword(password);
                
                passwordCache.put(id, pwd);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid password type: " + type);
            } catch (Exceptions.UndefinedPasswordException e) {
                System.err.println("Error creating password of type " + type + ": " + e.getMessage());
            }
        }
        
        System.out.println("Loaded " + passwordCache.size() + " passwords.");
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

public void cleanupCategoryReferences() {
    // Recursively clean up all categories to remove null password references
    cleanupCategoryReferencesRecursive(rootCategory);
}

private void cleanupCategoryReferencesRecursive(Category category) {
    // Get all password IDs
    List<String> passwordIds = new ArrayList<>(category.getPasswordIds());
    
    // Check each ID and remove if the password doesn't exist
    for (String id : passwordIds) {
        if (!passwordCache.containsKey(id) || passwordCache.get(id) == null) {
            category.getPasswordIds().remove(id);
            System.out.println("Removed reference to non-existent password ID: " + id);
        }
    }
    
    // Clean up children recursively
    for (PasswordCategory child : category.getChildren()) {
        if (child instanceof Category) {
            cleanupCategoryReferencesRecursive((Category) child);
        }
    }
}

public String PasswordCategory(String passwordId) {
    // Implement the method by calling the existing getPasswordCategory method
    return getPasswordCategory(passwordId);
}

public String savePasswordWithCategory(Passwords password, String categoryPath) {
    // First save the password to get its ID
    String id = savePassword(password);
    
    // If successfully saved, add it to the specified category
    if (id != null) {
        addPasswordToCategory(categoryPath, password);
    }
    
    return id;
}
}