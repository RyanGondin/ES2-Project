package Strategy;

import Interfaces.PasswordCategory;
import Composite.Category;
import Interfaces.Passwords;
import Interfaces.StorageStrategy;
import Memento.PasswordManagerCaretaker;
import Memento.PasswordManagerMemento;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class StorageAPI implements StorageStrategy {
    private StorageStrategy storageStrategy;
    private String lastAccessedPasswordId;
    private PasswordManagerCaretaker caretaker = new PasswordManagerCaretaker(10); // Store 10 states
    private String masterPassword;

    public StorageAPI(String masterPassword) throws IOException {
        this.masterPassword = masterPassword;
        
        File passwordsFile = new File("passwords.bin");
        File categoriesFile = new File("categories.bin");
        boolean filesExist = passwordsFile.exists() && categoriesFile.exists();
        
        // Create strategy without initializing state
        FileStorageStrategy fileStrategy = new FileStorageStrategy(masterPassword, false); // Pass false to skip initialization
        this.storageStrategy = fileStrategy;
        
        // Load data if master password provided and files exist
        if (masterPassword != null && !masterPassword.isEmpty() && filesExist) {
            fileStrategy.loadDataWithMasterPassword(masterPassword);
        }
        
        // Don't save state here! It would overwrite existing passwords
    }
    
    // Constructor for specifying strategy type
    public StorageAPI(String masterPassword, boolean usePostgres) throws IOException {
        this.masterPassword = masterPassword;
        if (usePostgres) {
            // Use placeholder values - these would be configured properly
            this.storageStrategy = new PostgresStorageStrategy(
                "jdbc:postgresql://localhost:5432/passwordmanager", 
                "postgres_user", 
                "postgres_password"
            );
        } else {
            this.storageStrategy = new FileStorageStrategy(masterPassword);
        }
    }
    
    // Allow changing the strategy at runtime
    public void setStorageStrategy(StorageStrategy strategy) {
        this.storageStrategy = strategy;
    }

    public StorageStrategy getStorageStrategy() {
        return this.storageStrategy;
    }

    // Implement StorageStrategy methods by delegating to current strategy
    
    @Override
    public String getPassword(String id) {
        this.lastAccessedPasswordId = id;
        saveState();
        return storageStrategy.getPassword(id);
    }
    
    @Override
    public String savePassword(Passwords password) {
        String id = storageStrategy.savePassword(password);
        this.lastAccessedPasswordId = id;
        saveState();
        return id;
    }

    public String savePasswordWithCategory(Passwords password, String categoryPath) {
        // First save the password to get its ID
        String id = storageStrategy.savePassword(password);
        
        // If category path is provided, add the existing password to that category
        if (categoryPath != null && !categoryPath.trim().isEmpty()) {
            if (storageStrategy instanceof FileStorageStrategy) {
                // Get the saved password to avoid creating duplicates
                Passwords savedPassword = getAllPasswords().get(id);
                storageStrategy.addPasswordToCategory(categoryPath, savedPassword);
            } else {
                storageStrategy.addPasswordToCategory(categoryPath, password);
            }
        } else {
            System.out.println("Password saved without category assignment.");
        }
        
        this.lastAccessedPasswordId = id;
        saveState();
        return id;
    }

    @Override
    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return storageStrategy.getAllPasswords();
    }

    @Override
    public Category getRootCategory() {
        return storageStrategy.getRootCategory();
    }

    @Override
    public void addPasswordToCategory(String categoryPath, Passwords password) {
        storageStrategy.addPasswordToCategory(categoryPath, password);
    }

    public void displayCategoryHierarchy() {
        // Clean up missing references first if using FileStorageStrategy
        if (storageStrategy instanceof FileStorageStrategy) {
            ((FileStorageStrategy) storageStrategy).cleanupCategoryReferences();
        }
        
        Category rootCategory = storageStrategy.getRootCategory();
        displayCategory(rootCategory, 0);
    }

    private void displayCategory(Category category, int depth) {
        // Create indentation based on depth
        String indent = "  ".repeat(depth);
        
        // Display category name
        System.out.println(indent + "- " + category.getName());
        
        // Display password IDs in this category
        List<String> passwordIds = category.getPasswordIds();
        if (!passwordIds.isEmpty()) {
            for (String id : passwordIds) {
                Passwords pwd = getAllPasswords().get(id);
                String name = (pwd != null) ? pwd.getName() : "[Missing Password]";
                System.out.println(indent + "  * " + name + " (ID: " + id + ")");
            }
        }
        
        // Recursively display children
        for (PasswordCategory child : category.getChildren()) {
            if (child instanceof Category) {
                displayCategory((Category) child, depth + 1);
            }
        }
    }

    public PasswordManagerCaretaker getCaretaker() {
        return caretaker;
    }

    @Override
    public void saveState() {
        storageStrategy.saveState();
        PasswordManagerMemento memento = createMemento();
        caretaker.addMemento(memento);
    }

    @Override
    public void restoreState(LinkedHashMap<String, Passwords> passwords, Category rootCategory) {
        storageStrategy.restoreState(passwords, rootCategory);
    }

    public PasswordManagerMemento createMemento() {
        return new PasswordManagerMemento(
            storageStrategy.getAllPasswords(), 
            storageStrategy.getRootCategory(),
            lastAccessedPasswordId
        );
    }

    public void restoreFromMemento(PasswordManagerMemento memento) {
        if (memento != null) {
            storageStrategy.restoreState(
                memento.getSavedPasswords(),
                memento.getSavedRootCategory()
            );
            this.lastAccessedPasswordId = memento.getLastAccessedPasswordId();
        }
    }

    public boolean verifyMasterPassword(String password) {
        return this.masterPassword.equals(password);
    }

    public void setMasterPassword(String newMasterPassword) throws IOException {
        this.masterPassword = newMasterPassword;
        
        // If current strategy is FileStorageStrategy, we need to recreate it
        if (storageStrategy instanceof FileStorageStrategy) {
            // Save current state if we have data
            LinkedHashMap<String, Passwords> currentPasswords = storageStrategy.getAllPasswords();
            Category currentRoot = storageStrategy.getRootCategory();
            boolean hasData = !currentPasswords.isEmpty();
            
            // Create new strategy with new password
            FileStorageStrategy newStrategy = new FileStorageStrategy(newMasterPassword, false); // Don't load automatically
            storageStrategy = newStrategy;
            
            // Only restore and save if we had data before
            if (hasData) {
                storageStrategy.restoreState(currentPasswords, currentRoot);
            }
        }
    }

    public String getPasswordCategory(String passwordId) {
        return storageStrategy.PasswordCategory(passwordId);
    }

    public String PasswordCategory(String passwordId) {
        // Call getPasswordCategory since this already exists and is implemented
        return getPasswordCategory(passwordId);
    }
}
