package Strategy;

import Composite.Category;
import Interfaces.Passwords;
import Interfaces.StorageStrategy;
import java.util.UUID;

public class PostgresStorageStrategy extends AbstractStorageStrategy {
    // Database connection details - to be implemented
    private String connectionString;
    private String username;
    private String password;
    
    public PostgresStorageStrategy(String connectionString, String username, String password) {
        super();
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
        
        // In the future, this will initialize the connection
        // For now, leave as placeholder
        
        loadFromDatabase();
    }
    
    public String getPassword(String id) {
        // Currently use in-memory cache
        // In the future, fetch from database if not in cache
        Passwords password = passwordCache.get(id);
        return password != null ? password.getPassword() : null;
    }
    
    public String savePassword(Passwords password) {
        String id = UUID.randomUUID().toString();
        passwordCache.put(id, password);
        return id;
    }
    
    public void addPasswordToCategory(String categoryPath, Passwords password) {

    }
    
    public void saveState() {

        System.out.println("PostgreSQL storage: state saving not yet implemented");
    }
    
    private void loadFromDatabase() {

        System.out.println("PostgreSQL storage: loading not yet implemented");
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