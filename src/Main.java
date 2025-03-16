import Interfaces.Passwords;
import Exceptions.UndefinedPasswordException;

public class Main {
    public static void main(String[] args) {
        try {
            Passwords strongPassword = FactoryPassword.makePassword(PasswordType.STRONG);
            strongPassword.setPassword("StrongPass123");
            strongPassword.show(); // Expected output: Password: StrongPass123

            Passwords standartPassword = FactoryPassword.makePassword(PasswordType.STANDART);
            standartPassword.setPassword("StandartPass123");
            standartPassword.show(); // Expected output: Password: StandartPass123
        } catch (UndefinedPasswordException e) {
            e.printStackTrace();
        }
    }
}