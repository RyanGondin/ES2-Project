import Interfaces.PasswordType;
import Interfaces.Passwords;

public class Strong implements Passwords {
    private String password;
    private PasswordType type;

    /**
     * Constructor to initialize.
     */
    protected Strong() {
        this.type = PasswordType.STRONG;
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

    public void show() {
        System.out.println("Password: " + this.password);
    }
}