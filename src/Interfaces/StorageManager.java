package Interfaces;

import Exceptions.ServiceNotFoundException;

public abstract class StorageManager {
    protected StorageImplementation storageImplementation;

    public StorageManager(StorageImplementation storageImplementation) {
        this.storageImplementation = storageImplementation;
    }

    public abstract String getStorage(String storageId) throws ServiceNotFoundException;

    public abstract String setStorage(String storage);
}