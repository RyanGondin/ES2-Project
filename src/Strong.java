import Interfaces.PasswordType;
import Interfaces.Passwords;

public class Strong implements Passwords {
    private String password;
    private final PasswordType type;
    private String username; // Field to store the username
    private String name;     // Field to store the name

    /**
     * Constructor for initialization.
     */
    protected Strong() {
        this.type = PasswordType.STRONG;
        this.password = generateStrongPassword(); // Automatically generate a strong password
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
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void show() {
        System.out.println("Password: " + this.password);
    }

    private String generateStrongPassword() {
        // Generate a strong password with uppercase, lowercase, digits, and special characters
        return PasswordGenerator.generatePassword(16, true, true, true);
    }
}
