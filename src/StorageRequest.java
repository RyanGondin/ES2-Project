import Exceptions.ServiceNotFoundException;
import Interfaces.StorageImplementation;
import Interfaces.StorageManager;

public class StorageRequest extends StorageManager {

    public StorageRequest(StorageImplementation storageImplementation) {
        super(storageImplementation);
    }

    @Override
    public String getStorage(String storageId) throws ServiceNotFoundException {
        String result = storageImplementation.getStorage(storageId);
        if (result == null) {
            throw new ServiceNotFoundException();
        }
        return result;
    }

    @Override
    public String setStorage(String storage) {
        return storageImplementation.setStorage(storage);
    }
}
