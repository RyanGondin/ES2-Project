package Decorator;


import Interfaces.Passwords;
import Interfaces.StorageStrategy;
import Strategy.StorageAPI;

import java.util.LinkedHashMap;

public class AlertStorageDecorator extends StorageDecorator {
    public AlertStorageDecorator(StorageStrategy storageStrategy) {
        super(storageStrategy);
    }

    public String savePassword(Passwords password) {
        String id = super.savePassword(password);
        sendAlert("Password armazenada com sucesso! ID: " + id);
        return id;
    }

    public String savePasswordWithCategory(Passwords password, String categoryPath) {
        String id = super.savePasswordWithCategory(password, categoryPath);
        sendAlert("Password armazenada com sucesso na categoria " + categoryPath + "! ID: " + id);
        return id;
    }

    private void sendAlert(String mensagem) {
        System.out.println("[ALERTA]: " + mensagem);
    }
}
