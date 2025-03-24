import Exceptions.ServiceNotFoundException;
import Interfaces.StorageImplementation;
import Interfaces.StorageManager;

public class Aggregator extends StorageManager {

    public Aggregator(StorageImplementation storageImplementation) {
        super(storageImplementation);
    }

    @Override
    public String getStorage(String storageId) throws ServiceNotFoundException {
        if (storageId.equals("0")) {
            return storageImplementation.getStorage("0"); // Aggregate all data
        } else {
            return storageImplementation.getStorage(storageId);
        }
    }

    @Override
    public String setStorage(String storage) {
        return storageImplementation.setStorage(storage);
    }
}
