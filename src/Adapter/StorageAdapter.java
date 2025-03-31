package Adapter;
import Interfaces.StorageImplementation;
import Interfaces.Passwords;
import Factory.Standart;
import Composite.Category;
import Interfaces.PasswordCategory; // Add this import
import Factory.FactoryPassword; // Add this import
import Interfaces.PasswordType; // Add this import
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.HashMap;

public class StorageAdapter implements StorageImplementation {
    private final FileStorage fileStorage;
    private final Encryptor encryptor;
    private LinkedHashMap<String, Passwords> storage;
    
    // Add hierarchical storage
    private Category rootCategory;
    private String lastAccessedPasswordId; // Add field for last accessed password ID

    public StorageAdapter(FileStorage fileStorage, String masterPassword, boolean loadStorage) {
        this.fileStorage = fileStorage;
        this.encryptor = new Encryptor(masterPassword);
        this.storage = new LinkedHashMap<>();
        this.rootCategory = new Category("Root");
        if (loadStorage) {
            loadStorage();
        }
    }

    // Keep the old constructor for compatibility
    public StorageAdapter(FileStorage fileStorage, String masterPassword) {
        this(fileStorage, masterPassword, true);
    }

    @Override
    public String getStorage(String storageId) {
        this.lastAccessedPasswordId = storageId;
        saveStorage();
        return storage.get(storageId) != null ? storage.get(storageId).getPassword() : null;
    }

    @Override
    public String setStorage(Passwords password) {
        // Generate a unique ID for the password
        String id = UUID.randomUUID().toString();
        
        // Store the password in the map
        storage.put(id, password);
        
        // Update the last accessed ID
        this.lastAccessedPasswordId = id;
        
        // Save the state to persistent storage
        saveStorage();
        
        return id;
    }

    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return new LinkedHashMap<>(storage);
    }
    
    // Add getter for root category
    public Category getRootCategory() {
        return rootCategory;
    }

    public void addPasswordToCategory(String categoryPath, Passwords password) {
        if (password == null) {
            // Just create the category structure without adding any password
            findOrCreateCategoryPath(categoryPath);
            saveStorage();
            return;
        }
        
        // First store in flat storage
        String id = UUID.randomUUID().toString();
        storage.put(id, password);
        
        // Then add to category
        Category targetCategory = findOrCreateCategoryPath(categoryPath);
        
        // Check if password implements PasswordCategory before casting
        if (password instanceof PasswordCategory) {
            targetCategory.add((PasswordCategory)password);
        } else {
            System.err.println("Warning: Password doesn't implement PasswordCategory interface");
        }
        
        saveStorage();
    }

    private Category findOrCreateCategoryPath(String path) {
        if (path == null || path.isEmpty()) {
            return rootCategory;
        }
        
        String[] segments = path.split("/");
        Category current = rootCategory;
        
        for (String segment : segments) {
            boolean found = false;
            for (PasswordCategory child : current.getChildren()) {
                if (child instanceof Category && ((Category)child).getName().equals(segment)) {
                    current = (Category)child;
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                Category newCategory = new Category(segment);
                current.add(newCategory);
                current = newCategory;
            }
        }
        
        return current;
    }

    private void loadStorage() {
        try {
            if (fileStorage.exists()) {
                String data = fileStorage.readData();
                if (!data.trim().isEmpty()) {
                    try {
                        String decryptedData;
                        // Handle all version formats
                        if (data.startsWith("v3|")) {
                            // Current hierarchical format
                            decryptedData = encryptor.decrypt(data.substring(3));
                        } else if (data.startsWith("v2:")) {
                            // Previous format
                            decryptedData = encryptor.decrypt(data.substring(3));
                        } else {
                            // Legacy format (no version prefix)
                            throw new IOException("Incompatible encryption format");
                        }
                        deserializePasswords(decryptedData);
                    } catch (Exception e) {
                        System.err.println("Encryption error details: ");
                        e.printStackTrace();
                        System.err.println("Creating empty password storage due to decryption error.");
                        storage = new LinkedHashMap<>();
                    }
                }
            } else {
                fileStorage.create();
            }
        } catch (IOException e) {
            System.err.println("Error loading storage: " + e.getMessage());
        }
    }

    private void saveStorage() {
        try {
            String serializedData = serializePasswords();
            // Fix the version format - use v3| instead of v2:
            String encryptedData = "v3|" + encryptor.encrypt(serializedData);
            fileStorage.writeData(encryptedData);
        } catch (IOException e) {
            System.err.println("Error saving storage: " + e.getMessage());
        }
    }

    private String serializePasswords() {
        StringBuilder builder = new StringBuilder();
        // Write format version
        builder.append("v3|"); // Versioning for hierarchical format
        
        // First, serialize all individual passwords (for backward compatibility)
        builder.append("PASSWORDS\n");
        for (var entry : storage.entrySet()) {
            Passwords password = entry.getValue();
            builder.append(String.format("%s,%s,%s,%s,%s\n",
                entry.getKey(),
                password.getName(),
                password.getUsername(),
                password.getPassword(),
                password.getType())); // Store the password type
        }
        
        // Then serialize the hierarchy
        builder.append("HIERARCHY\n");
        serializeCategoryRecursive(rootCategory, "", builder);
        
        return builder.toString();
    }

    private void serializeCategoryRecursive(PasswordCategory category, String path, StringBuilder builder) {
        if (category instanceof Category) {
            Category cat = (Category) category;
            
            // Skip the root category with empty path to avoid redundancy
            if (!path.isEmpty() || !cat.getName().equals("Root")) {
                builder.append(String.format("CAT,%s,%s\n", path, cat.getName()));
            }
            
            String newPath = path.isEmpty() ? cat.getName() : path + "/" + cat.getName();
            // For Root category, use empty path
            if (cat.getName().equals("Root")) {
                newPath = "";
            }
            
            for (PasswordCategory child : cat.getChildren()) {
                serializeCategoryRecursive(child, newPath, builder);
            }
        } else if (category instanceof Passwords) {
            // Reference existing password by ID
            String id = findPasswordId((Passwords)category);
            if (id != null) {
                // Format: PWD,path,id
                builder.append(String.format("PWD,%s,%s\n", path, id));
            }
        }
    }

    private String findPasswordId(Passwords password) {
        for (var entry : storage.entrySet()) {
            if (entry.getValue() == password) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void deserializePasswords(String data) {
        storage.clear();
        rootCategory = new Category("Root");
        
        // Check for version header
        if (data.startsWith("v3|")) {
            // Remove version header
            data = data.substring(3);
            
            // Split into sections
            String[] sections = data.split("HIERARCHY\n", 2);
            String passwordsSection = sections[0].replace("PASSWORDS\n", "");
            
            // First process all passwords
            String[] passwordLines = passwordsSection.split("\n");
            for (String line : passwordLines) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String id = parts[0];
                        String name = parts[1];
                        String username = parts[2];
                        String passwordValue = parts[3];
                        String typeStr = parts[4];
                        
                        Passwords password;
                        try {
                            PasswordType type = PasswordType.valueOf(typeStr);
                            password = FactoryPassword.makePassword(type);
                        } catch (Exception e) {
                            // Fallback to standard
                            password = new Standart();
                        }
                        
                        password.setName(name);
                        password.setUsername(username);
                        password.setPassword(passwordValue);
                        storage.put(id, password);
                    }
                }
            }
            
            // Then process hierarchy if available
            if (sections.length > 1) {
                String hierarchySection = sections[1];
                String[] hierarchyLines = hierarchySection.split("\n");
                
                // Map to store path -> category for easier lookup
                HashMap<String, Category> pathMap = new HashMap<>();
                pathMap.put("", rootCategory);
                
                // First pass: create all categories
                for (String line : hierarchyLines) {
                    if (line.startsWith("CAT,")) {
                        String[] parts = line.split(",", 3);
                        if (parts.length == 3) {
                            String path = parts[1];
                            String catName = parts[2];
                            
                            Category newCategory = new Category(catName);
                            
                            // Store in path map for child lookups
                            String fullPath = path.isEmpty() ? catName : path + "/" + catName;
                            pathMap.put(fullPath, newCategory);
                            
                            // Add to parent
                            Category parent = pathMap.get(path);
                            if (parent != null) {
                                parent.add(newCategory);
                            } else {
                                rootCategory.add(newCategory);
                            }
                        }
                    }
                }
                
                // Second pass: add passwords to categories
                for (String line : hierarchyLines) {
                    if (line.startsWith("PWD,")) {
                        String[] parts = line.split(",", 3);
                        if (parts.length == 3) {
                            String path = parts[1];
                            String id = parts[2];
                            
                            // Get the category and password
                            Category parent = pathMap.get(path);
                            Passwords password = storage.get(id);
                            
                            if (parent != null && password != null) {
                                parent.add((PasswordCategory)password);
                            }
                        }
                    }
                }
            }
        } else {
            // Handle legacy format (your existing code)
            String[] lines = data.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length == 4) {
                        Standart password = new Standart();
                        password.setName(parts[1]);
                        password.setUsername(parts[2]);
                        password.setPassword(parts[3]);
                        storage.put(parts[0], password);
                    }
                }
            }
        }
    }

    public void restoreState(LinkedHashMap<String, Passwords> savedPasswords, Category savedRootCategory) {
        // Deep copy the passwords to restore
        this.storage = new LinkedHashMap<>(savedPasswords);
        
        // Replace the category structure
        this.rootCategory = savedRootCategory;
        
        // Persist the restored state
        saveStorage();
    }
}