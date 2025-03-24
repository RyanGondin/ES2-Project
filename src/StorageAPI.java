import Interfaces.Storage;

import java.util.LinkedHashMap;
import java.util.UUID;

public class StorageAPI implements Storage {
    protected LinkedHashMap<String, String> storage = new LinkedHashMap<String, String>();
    private String storageId;

    public  StorageAPI() {
    }

    public String getStorage(String storageId) {
        if (storageId.equals("0")) {
            String agg = "";
            for (String key : storage.keySet()) {
                agg += storage.get(key);
            }
            return agg;
        } else {
            return this.storage.get(storageId);
        }
    }

    public String setStorage(String storage) {
        String id = UUID.randomUUID().toString();
        this.storage.put(id, storage);
        return id;
    }
}
