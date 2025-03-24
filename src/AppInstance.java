public class AppInstance {
    // Eager initialization
    private static final AppInstance instance = new AppInstance();

    // Private constructor to prevent instantiation from outside
    private AppInstance() {
    }

    // Public method to provide access to the instance
    public static AppInstance getInstance() {
        return instance;
    }

    // Use ConfigurationFactory to get the ConfigurationManager instance
    public ConfigurationManager getConfigurationManager() {
        return ConfigurationFactory.getConfigurationManager();
    }
}