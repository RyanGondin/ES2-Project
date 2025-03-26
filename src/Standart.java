import Interfaces.PasswordType;
import Interfaces.Passwords;
import Interfaces.PasswordCategory;
import java.util.List;

public class Standart implements Passwords, PasswordCategory {
    private String password;
    private final PasswordType type;
    private String name; // Name of the service
    private String username; // Username associated with the password

    public Standart() {
        this.type = PasswordType.STANDART;
        this.password = generateStandartPassword(); // Automatically generate a standard password
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
        // Generate a standard password with lowercase and digits only
        return PasswordGenerator.generatePassword(8, false, true, false);
    }
}
