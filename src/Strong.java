import Interfaces.PasswordType;
import Interfaces.Passwords;
import Interfaces.PasswordCategory;
import java.util.List;

public class Strong implements Passwords, PasswordCategory {
    private String password;
    private PasswordType type;

    /**
     * Construtor para inicialização.
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

    @Override
    public void show() {
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
}
