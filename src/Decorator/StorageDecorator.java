
package Decorator;
import Composite.Category;
import Interfaces.Passwords;
import Interfaces.StorageStrategy;

import java.util.LinkedHashMap;

public abstract class StorageDecorator implements StorageStrategy {

    private String id;
    private StorageStrategy storageStrategy;

    public StorageDecorator( StorageStrategy storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    public String getPassword(String id) {
        return storageStrategy.getPassword(id);
    }
    public String savePassword(Passwords password) {
        return storageStrategy.savePassword(password);
    }
    public LinkedHashMap<String, Passwords> getAllPasswords() {
        return storageStrategy.getAllPasswords();
    }

    public Category getRootCategory() {
        return storageStrategy.getRootCategory();
    }

    public void addPasswordToCategory(String categoryPath, Passwords password) {
        storageStrategy.addPasswordToCategory(categoryPath, password);
    }

    public void saveState() {
        storageStrategy.saveState();
    }

    public void restoreState(LinkedHashMap<String, Passwords> passwords, Category rootCategory) {
        storageStrategy.restoreState(passwords, rootCategory);
    }

    public String PasswordCategory(String passwordId) {
        return storageStrategy.PasswordCategory(passwordId);
    }

}
