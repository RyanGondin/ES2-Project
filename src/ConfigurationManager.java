import java.util.HashMap;

public class ConfigurationManager {
    private HashMap<String, String> configuration = new HashMap<>();

    public String getConnectionString(String key) {
        return this.configuration.get(key);
    }

    public void setConnectionString(String key, String value) {
        this.configuration.put(key, value);
    }
}