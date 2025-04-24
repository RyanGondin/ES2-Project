package Decorator;

import Composite.Category;
import Interfaces.Passwords;
import Interfaces.StorageStrategy;

import java.util.LinkedHashMap;

public abstract class StorageDecorator implements StorageStrategy {
    protected final StorageStrategy storageStrategy;

    public StorageDecorator(StorageStrategy storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    @Override
    public String getPassword(String id) {
        return storageStrategy.getPassword(id);
    }

    @Override
    public String savePassword(Passwords password) {
        return storageStrategy.savePassword(password);
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

    @Override
    public void saveState() {
        storageStrategy.saveState();
    }

    @Override
    public void restoreState(LinkedHashMap<String, Passwords> passwords, Category rootCategory) {
        storageStrategy.restoreState(passwords, rootCategory);
    }

    @Override
    public String PasswordCategory(String passwordId) {
        return storageStrategy.PasswordCategory(passwordId);
    }
}
