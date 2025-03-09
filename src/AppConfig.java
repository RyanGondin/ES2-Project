import java.util.HashMap;

public class AppConfig {

    private static AppConfig instance;
    private HashMap<String, String> configuration = new HashMap<String, String>();
    private String connectionString;

    private AppConfig(){

    }

    public static AppConfig getInstance(){

        if (instance == null){
            instance = new AppConfig();
        }
        return instance;

    }

    public String getConnectionString(String key){
        return this.configuration.get(key);
    }

    public void setConnectionString(String key, String value){
        this.configuration.put(key, value);
    }
}