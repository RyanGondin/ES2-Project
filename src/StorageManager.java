import Exceptions.ServiceNotFoundException;
import Interfaces.StorageImplementation;
import Interfaces.Passwords;


public abstract class StorageManager {
    private static volatile StorageManager instance; // Static instance for Singleton
    protected StorageImplementation storageImplementation;

    // Private constructor to prevent instantiation
    protected StorageManager(StorageImplementation storageImplementation) {
        this.storageImplementation = storageImplementation;
    }

    // Public static method to provide access to the single instance
    public static StorageManager getInstance(StorageImplementation storageImplementation) {
        if (instance == null) {
            synchronized (StorageManager.class) { // Double-checked locking for thread safety
                if (instance == null) {
                    instance = new StorageRequest(storageImplementation); // Default implementation
                }
            }
        }
        return instance;
    }

    // Abstract methods to be implemented by subclasses
    public abstract String getStorage(String storageId) throws ServiceNotFoundException;

    public abstract String setStorage(Passwords password); // Updated to match the interface
}