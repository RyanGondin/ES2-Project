import Exceptions.UndefinedPasswordException;
import Interfaces.Passwords;
import Interfaces.PasswordType;

/**
 * A factory abstract class for creating different types of passwords.
 */
public abstract class FactoryPassword {

    /**
     * Creates an instance of a password based on the given type.
     * @param type the type of password to create
     * @return the created password
     * @throws UndefinedPasswordException if the type is invalid
     */
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
