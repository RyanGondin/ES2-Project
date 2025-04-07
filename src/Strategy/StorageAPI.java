package Strategy;

import Composite.Category;
import Interfaces.Passwords;
import Interfaces.StorageStrategy;
import Memento.PasswordManagerCaretaker;
import Memento.PasswordManagerMemento;
import java.io.IOException;
import java.util.LinkedHashMap;

public class StorageAPI implements StorageStrategy {
    private StorageStrategy storageStrategy;
    private String lastAccessedPasswordId;
    private PasswordManagerCaretaker caretaker = new PasswordManagerCaretaker(10); // Store 10 states
    private String masterPassword;

    public StorageAPI(String masterPassword) throws IOException {
        this.masterPassword = masterPassword;
        // Default to file storage
        this.storageStrategy = new FileStorageStrategy(masterPassword);
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
        Category root = storageStrategy.getRootCategory();
        root.show();
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
        // with the new master password for encryption
        if (storageStrategy instanceof FileStorageStrategy) {
            // Save current state
            LinkedHashMap<String, Passwords> currentPasswords = storageStrategy.getAllPasswords();
            Category currentRoot = storageStrategy.getRootCategory();
            
            // Create new strategy with new password
            storageStrategy = new FileStorageStrategy(newMasterPassword);
            
            // Restore state
            storageStrategy.restoreState(currentPasswords, currentRoot);
        }
        
        saveState();
    }
}
