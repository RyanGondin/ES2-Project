import Exceptions.ServiceNotFoundException;
import Interfaces.StorageImplementation;
import Interfaces.Passwords;
import java.util.LinkedHashMap;

public abstract class StorageManager {
    protected StorageImplementation storageImplementation;

    // Private constructor to prevent instantiation
    protected StorageManager(StorageImplementation storageImplementation) {
        this.storageImplementation = storageImplementation;
    }

    // Abstract methods to be implemented by subclasses
    public abstract String getStorage(String storageId) throws ServiceNotFoundException;

    public abstract String setStorage(Passwords password);

    public abstract LinkedHashMap<String, Passwords> getAllPasswords(); // New method
}