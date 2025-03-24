/**
 * A factory abstract class for creating and managing a single instance of ConfigurationManager.
 */
public abstract class ConfigurationFactory {
    // Static instance of ConfigurationManager
    private static ConfigurationManager instance;

    /**
     * Provides a single instance of ConfigurationManager.
     * @return the single instance of ConfigurationManager
     */
    public static ConfigurationManager getConfigurationManager() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
}
