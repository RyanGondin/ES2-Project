package Decorator;


import Interfaces.Passwords;
import Interfaces.StorageStrategy;
import Strategy.StorageAPI;

import java.util.LinkedHashMap;

public class AlertStorageDecorator extends StorageDecorator {
    public AlertStorageDecorator(StorageStrategy storageStrategy) {
        super(storageStrategy);
    }

    public String savePasswword(Passwords password) {
        String id = super.savePassword(password);
        sendAlert("Password armazenada com sucesso! ID: " + id);
        return id;
    }

    private void sendAlert(String mensagem) {
        System.out.println("[ALERTA]: " + mensagem);
    }
}
