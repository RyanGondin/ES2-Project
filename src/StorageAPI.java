import Interfaces.StorageImplementation;

import java.util.LinkedHashMap;
import java.util.UUID;

public class StorageAPI implements StorageImplementation {
    private LinkedHashMap<String, String> storage = new LinkedHashMap<>();

    @Override
    public String getStorage(String storageId) {
        if (storageId.equals("0")) {
            StringBuilder agg = new StringBuilder();
            for (String key : storage.keySet()) {
                agg.append(storage.get(key));
            }
            return agg.toString();
        } else {
            return this.storage.get(storageId);
        }
    }

    @Override
    public String setStorage(String storage) {
        String id = UUID.randomUUID().toString();
        this.storage.put(id, storage);
        return id;
    }
}
