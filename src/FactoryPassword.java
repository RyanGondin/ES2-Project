import Exceptions.UndefinedPasswordException;
import Interfaces.Passwords;
import Interfaces.PasswordType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A factory abstract class for creating different types of passwords.
 */
public abstract class FactoryPassword {
    private static final Map<PasswordType, Supplier<? extends Passwords>> registry = new HashMap<>();

    static {
        registry.put(PasswordType.STANDART, Standart::new);
        registry.put(PasswordType.STRONG, Strong::new);
    }

    /**
     * Creates an instance of a password based on the given type.
     * @param type the type of password to create
     * @return the created password
     * @throws UndefinedPasswordException if the type is invalid
     */
    public static Passwords makePassword(PasswordType type) throws UndefinedPasswordException {
        Supplier<? extends Passwords> supplier = registry.get(type);
        if (supplier == null) {
            throw new UndefinedPasswordException("Invalid Password type: " + type);
        }
        return supplier.get();
    }
}
