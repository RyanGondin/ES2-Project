import Interfaces.Passwords;

public class Strong implements Passwords {
    private String password;
    private String type;

    protected Strong(){

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
        return ("Standart");
    }

    public void show(){
        System.out.println("Password: " );
    }
}
