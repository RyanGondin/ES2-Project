import Exceptions.ServiceNotFoundException;
import Interfaces.Storage;

import java.util.HashMap;
import java.util.UUID;

public class StorageRequest {
    protected HashMap<String, StorageAPI> services = new HashMap<String, StorageAPI>();
    public StorageRequest(){

    }

    public String getStorage(String serviceId, String storageId) throws ServiceNotFoundException {
        if(this.services.containsKey(serviceId)) {
            return this.services.get(serviceId).getStorage(storageId);
        }else throw new ServiceNotFoundException();
    }
    public String setStorage(String serviceId, String storage) throws ServiceNotFoundException {
        if(this.services.containsKey(serviceId)) {
            return this.services.get(serviceId).setStorage(storage);
        }else throw new ServiceNotFoundException();
    }

    public String addService(Storage service) {
        String id = UUID.randomUUID().toString();
        this.services.put(id, service);

        return id;
    }
}
