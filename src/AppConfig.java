import java.util.HashMap;

public class AppConfig {
    // Eager initialization
    private static final AppConfig instance = new AppConfig();
    private HashMap<String, String> configuration = new HashMap<>();

    // Private constructor to prevent instantiation
    private AppConfig() {}

    // Public method to provide access to the instance
    public static AppConfig getInstance() {
        return instance;
    }

    public String getConnectionString(String key) {
        return this.configuration.get(key);
    }

    public void setConnectionString(String key, String value) {
        this.configuration.put(key, value);
    }
}