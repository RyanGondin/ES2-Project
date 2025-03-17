public class AppInstance {
    // Eager initialization
    private static final AppInstance instance = new AppInstance();
    private ConfigurationManager configurationManager = new ConfigurationManager();

    // Private constructor to prevent instantiation
    private AppInstance() {}

    // Public method to provide access to the instance
    public static AppInstance getInstance() {
        return instance;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
}