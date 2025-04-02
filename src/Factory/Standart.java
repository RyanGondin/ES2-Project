package Factory;
import Exceptions.PoolExhaustedException;
import Interfaces.PasswordType;
import Interfaces.Passwords;
import Interfaces.PasswordCategory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import Composite.PasswordGenerator;
import ReusablePool.ReusablePool;

public class Standart implements Passwords, PasswordCategory {
    private String password;
    private final PasswordType type;
    private String name; // Name of the service
    private String username; // Username associated with the password

    public Standart() {
        this.type = PasswordType.STANDART;
        this.password = generateStandartPassword(); // Automatically generate a standard password
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

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public PasswordType getType() {
        return this.type;
    }

    @Override
    public void show() {
        System.out.println("Name: " + this.name);
        System.out.println("Username: " + this.username);
        System.out.println("Password: " + this.password);
        System.out.println("Type: Standard (Medium Security)");
    }

    @Override
    public void add(PasswordCategory category) {
        throw new UnsupportedOperationException("Cannot add a category to a password.");
    }

    @Override
    public void remove(PasswordCategory category) {
        throw new UnsupportedOperationException("Cannot remove a category from a password.");
    }

    @Override
    public List<PasswordCategory> getChildren() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    private String generateStandartPassword() {
        // Standard password: 12 characters, lowercase and digits only
        // Medium security level for regular accounts
        return PasswordGenerator.generatePassword(12, false, true, false);
    }
    public void saveToDb() throws IOException, PoolExhaustedException {
        HttpURLConnection connection = dbConnection.acquire(dbUrl);
        // do nothing
    }
}
