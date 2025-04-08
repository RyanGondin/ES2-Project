package Decorator;

import Adapter.StorageAPI;
import Interfaces.Passwords;
import java.util.LinkedHashMap;

public class AlertStorageDecorator extends StorageDecorator {
    public AlertStorageDecorator(StorageAPI storageAPI) {
        super(storageAPI);
    }

    @Override
    public String setStorage(Passwords password) {
        String id = super.setStorage(password);
        enviarAlerta("Senha armazenada com sucesso! ID: " + id);
        return id;
    }

    @Override
    public LinkedHashMap<String, Passwords> getAllPasswords() { // Mesma assinatura de StorageAPI
        enviarAlerta("Lista de senhas acessada.");
        return super.getAllPasswords();
    }

    private void enviarAlerta(String mensagem) {
        System.out.println("[ALERTA]: " + mensagem);
    }
}
