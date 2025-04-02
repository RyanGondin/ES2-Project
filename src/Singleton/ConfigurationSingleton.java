package Singleton;

/**
 * A factory abstract class for creating and managing a single instance of ConfigurationManager.
 */
public abstract class ConfigurationSingleton {
    private static volatile ConfigurationManager instance;

    /**
     * Provides a single instance of ConfigurationManager.
     * @return the single instance of ConfigurationManager
     */
    public static ConfigurationManager getConfigurationManager() {
        if (instance == null) {
            synchronized (ConfigurationSingleton.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }
}
