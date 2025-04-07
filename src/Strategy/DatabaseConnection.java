package Strategy;

import Composite.Category;
import Interfaces.Passwords;
import java.util.LinkedHashMap;

public class DatabaseConnection {
    private String connectionString;
    private String username;
    private String password;
    
    public DatabaseConnection(String connectionString, String username, String password) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
    }
    
    public Passwords getPasswordById(String id) {
        return null;
    }
    
    public void savePassword(String id, Passwords password) {
    }
    
    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return new LinkedHashMap<>();
    }
    
    public void saveAllPasswords(LinkedHashMap<String, Passwords> passwords) {
    }
    
    public Category getCategoryStructure() {
        return null;
    }
    
    public void saveCategoryStructure(Category rootCategory) {
    }
    public void close() {
    }
}
