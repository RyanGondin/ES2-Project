package Factory;
import Composite.PasswordGenerator;
import Exceptions.PoolExhaustedException;
import Interfaces.PasswordType;
import Interfaces.Passwords;
import Interfaces.PasswordCategory;
import ReusablePool.ReusablePool;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;

public class Strong implements Passwords, PasswordCategory {
    private String password;
    private final PasswordType type;
    private String username;
    private String name;

    public Strong() { // Changed from protected to public for consistency
        this.type = PasswordType.STRONG;
        this.password = generateStrongPassword();
    }

    private ReusablePool dbConnection = ReusablePool.getInstance();

    private URL dbUrl;
    {
        try {
            dbUrl = new URL("http://localhost:8080/db");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PasswordType getType() {
        return this.type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void show() {
        System.out.println("Name: " + this.name);
        System.out.println("Username: " + this.username);
        System.out.println("Password: " + this.password);
        System.out.println("Type: Strong (High Security)");
    }

    private String generateStrongPassword() {
        // Strong password: 24 characters with all character types
        // High security level for sensitive accounts
        return PasswordGenerator.generatePassword(24, true, true, true);
    }

    public void add(PasswordCategory category) {
        throw new UnsupportedOperationException("Cannot add a category to a password.");
    }

    public void remove(PasswordCategory category) {
        throw new UnsupportedOperationException("Cannot remove a category from a password.");
    }

    public List<PasswordCategory> getChildren() {
        return null;
    }

    public void saveToDb() throws IOException, PoolExhaustedException {
        HttpURLConnection connection = dbConnection.acquire(dbUrl);
        // do nothing
    }

    public static void displayAllPasswords(LinkedHashMap<String, Passwords> passwords) {
        System.out.println("\nAll Passwords:");
        passwords.forEach((id, password) -> {
            // Add null check before accessing methods
            if (password != null) {
                System.out.println("ID: " + id + ", Name: " + password.getName());
            } else {
                System.out.println("ID: " + id + ", Name: [NULL PASSWORD OBJECT]");
            }
        });
    }
}
