import Exceptions.ServiceNotFoundException;

public class Aggregator extends StorageRequest{
    public Aggregator() {
    }
    public String getStorage(String serviceId, String storageId) throws ServiceNotFoundException{
        if(this.services.containsKey(serviceId)) {
            return this.services.get(serviceId).getStorage("0");
        }else throw new ServiceNotFoundException();
    }
}
