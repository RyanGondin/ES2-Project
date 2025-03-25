import Exceptions.ServiceNotFoundException;
import Interfaces.StorageImplementation;
import Interfaces.Passwords;

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
    public String setStorage(Passwords password) {
        return storageImplementation.setStorage(password); // Delegate to the implementation
    }
}
