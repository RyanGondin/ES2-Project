package Decorator;

import Interfaces.Passwords;
import Interfaces.StorageStrategy;

public class AlertStorageDecorator extends StorageDecorator {
    public AlertStorageDecorator(StorageStrategy storageStrategy) {
        super(storageStrategy);
    }

    @Override
    public String savePassword(Passwords password) {
        String id = super.savePassword(password);
        sendAlert("Password stored successfully! ID: " + id);
        return id;
    }

    @Override
    public String savePasswordWithCategory(Passwords password, String categoryPath) {
        String id = savePasswordWithCategory(password, categoryPath);
        sendAlert("Password stored successfully in category " + categoryPath + "! ID: " + id);
        return id;
    }

    @Override
    public String PasswordCategory(String passwordId) {
        return super.PasswordCategory(passwordId);
    }

    private void sendAlert(String message) {
        System.out.println("[ALERT]: " + message);
    }
}
