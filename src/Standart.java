import Interfaces.PasswordType;
import Interfaces.Passwords;

public class Standart implements Passwords {
    private String password;
    private PasswordType type;

    /**
     * Constructor to initialize.
     */
    protected Standart() {
        this.type = PasswordType.STANDART;
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
        System.out.println("Password: " + this.password);
    }
}