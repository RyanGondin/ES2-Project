import Exceptions.UndefinedPasswordException;
import Interfaces.Passwords;


public abstract class FactoryPassword {

    public static Passwords makePassword(PasswordType type) throws UndefinedPasswordException {
        switch (type) {
            case STANDART:
                return new Standart();
            case STRONG:
                return new Strong();
            default:
                throw new UndefinedPasswordException("Invalid Password type: " + type);
        }
    }
}
