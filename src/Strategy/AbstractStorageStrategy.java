package Strategy;

import Composite.Category;
import Interfaces.PasswordCategory;
import Interfaces.Passwords;
import Interfaces.StorageStrategy;
import java.util.LinkedHashMap;

public abstract class AbstractStorageStrategy implements StorageStrategy {
    protected LinkedHashMap<String, Passwords> passwordCache;
    protected Category rootCategory; // Keep as Category for addPasswordId method access
    
    public AbstractStorageStrategy() {
        this.passwordCache = new LinkedHashMap<>();
        this.rootCategory = new Category("Root");
    }
    
    @Override
    public Category getRootCategory() {
        return rootCategory;
    }
    
    @Override
    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return new LinkedHashMap<>(passwordCache);
    }
    
    @Override
    public void restoreState(LinkedHashMap<String, Passwords> passwords, Category rootCategory) {
        this.passwordCache = new LinkedHashMap<>(passwords);
        this.rootCategory = rootCategory;
        saveState(); // Ensure derived class handles persistence
    }
}