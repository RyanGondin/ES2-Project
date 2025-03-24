import Exceptions.UndefinedPasswordException;
import Interfaces.Passwords;
import Interfaces.PasswordType;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory abstract class for creating different types of passwords.
 */
public abstract class FactoryPassword {
    private static final Map<PasswordType, Class<? extends Passwords>> registry = new HashMap<>();

    static {
        registry.put(PasswordType.STANDART, Standart.class);
        registry.put(PasswordType.STRONG, Strong.class);
    }

    /**
     * Creates an instance of a password based on the given type.
     * @param type the type of password to create
     * @return the created password
     * @throws UndefinedPasswordException if the type is invalid
     */
    public static Passwords makePassword(PasswordType type) throws UndefinedPasswordException {
        Class<? extends Passwords> passwordClass = registry.get(type);
        if (passwordClass == null) {
            throw new UndefinedPasswordException("Invalid Password type: " + type);
        }
        try {
            return passwordClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new UndefinedPasswordException("Error creating password: " + e.getMessage());
        }
    }
}
