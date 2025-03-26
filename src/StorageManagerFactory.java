import Interfaces.StorageImplementation;
public class StorageManagerFactory {
    private static volatile StorageManager instance;

    public static StorageManager getInstance(StorageImplementation storageImplementation, boolean useAggregator) {
        if (instance == null) {
            synchronized (StorageManagerFactory.class) {
                if (instance == null) {
                    instance = useAggregator
                        ? new Aggregator(storageImplementation)
                        : new StorageRequest(storageImplementation);
                }
            }
        }
        return instance;
    }
}