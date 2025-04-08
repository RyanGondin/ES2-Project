package Interfaces;

import Composite.Category;
import java.util.LinkedHashMap;

public interface StorageStrategy {
    String getPassword(String id);
    String savePassword(Passwords password);
    LinkedHashMap<String, Passwords> getAllPasswords();
    Category getRootCategory();
    void addPasswordToCategory(String categoryPath, Passwords password);
    void saveState();
    void restoreState(LinkedHashMap<String, Passwords> passwords, Category rootCategory);
    String PasswordCategory(String passwordId);
}
