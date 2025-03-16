import Interfaces.Passwords;

public class Strong implements Passwords {
    private String password;
    private String type;

    protected Strong() {
        this.type = "Strong";
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
    public String getType() {
        return this.type;
    }

    public void show() {
        System.out.println("Password: " + this.password);
    }
}