
package Decorator;

import Adapter.StorageAPI;
import Interfaces.Passwords;
import java.util.LinkedHashMap;

public abstract class StorageDecorator extends StorageAPI {
    protected StorageAPI storageAPI;

    public StorageDecorator(StorageAPI storageAPI) {
        super(storageAPI.getMasterPassword()); 
        this.storageAPI = storageAPI;
    }

    @Override
    public LinkedHashMap<String, Passwords> getAllPasswords() { // Retorno correto
        return storageAPI.getAllPasswords();
    }
}
