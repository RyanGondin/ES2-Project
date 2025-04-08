package Strategy;

import Composite.Category;
import Interfaces.PasswordCategory;
import Interfaces.Passwords;
import Interfaces.StorageStrategy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public abstract class AbstractStorageStrategy implements StorageStrategy {
    protected LinkedHashMap<String, Passwords> passwordCache;
    protected Category rootCategory; // Keep as Category for addPasswordId method access
    
    public AbstractStorageStrategy() {
        this.passwordCache = new LinkedHashMap<>();
        this.rootCategory = new Category("Root");
        
        // Clean up any null entries when loaded
        cleanupNullPasswords();
    }
    
    /**
     * Removes any null password entries from the password cache
     */
    protected void cleanupNullPasswords() {
        if (passwordCache != null) {
            List<String> keysToRemove = new ArrayList<>();
            
            // Find keys with null values
            for (Map.Entry<String, Passwords> entry : passwordCache.entrySet()) {
                if (entry.getValue() == null) {
                    keysToRemove.add(entry.getKey());
                }
            }
            
            // Remove them
            for (String key : keysToRemove) {
                passwordCache.remove(key);
            }
        }
    }
    
    public Category getRootCategory() {
        return rootCategory;
    }
    
    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return new LinkedHashMap<>(passwordCache);
    }
    
    public void restoreState(LinkedHashMap<String, Passwords> passwords, Category rootCategory) {
        this.passwordCache = new LinkedHashMap<>(passwords);
        this.rootCategory = rootCategory;
        saveState();
    }
    
    public String getPasswordCategory(String passwordId) {
        // Find category containing this password ID
        return findPasswordCategory(rootCategory, passwordId, "");
    }

    private String findPasswordCategory(Category category, String passwordId, String path) {
        String currentPath = path.isEmpty() ? category.getName() : path + "/" + category.getName();
        
        // Check if this category directly contains the password ID
        if (category.containsPasswordId(passwordId)) {
            return currentPath;
        }
        
        // Check child categories recursively
        for (PasswordCategory child : category.getChildren()) {
            if (child instanceof Category) {
                String result = findPasswordCategory((Category)child, passwordId, currentPath);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null; // Not found in this branch
    }

    public String savePasswordWithCategory(Passwords password, String categoryPath) {
        String id = savePassword(password);
        if (id != null) {
            addPasswordToCategory(categoryPath, password);
        }
        return id;
    }
}