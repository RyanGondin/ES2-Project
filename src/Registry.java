import java.util.HashMap;

public class Registry {

    private static Registry instance;
    private HashMap<String, String> configuration = new HashMap<String, String>();
    private String connectionString;

    private Registry(){

    }

    public static Registry getInstance(){

        if (instance == null){
            instance = new Registry();
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