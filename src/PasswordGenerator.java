import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    private static final String ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;

    private static final SecureRandom random = new SecureRandom();

    private PasswordGenerator() {
        // Prevent instantiation
    }

    /**
     * Generates a random password with the specified length.
     * @param length the length of the password
     * @return the generated password
     */
    public static String generatePassword(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Password length must be at least 1");
        }

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALL_CHARACTERS.length());
            password.append(ALL_CHARACTERS.charAt(index));
        }
        return password.toString();
    }

    /**
     * Generates a random password with specific character requirements.
     * @param length the length of the password
     * @param includeUppercase whether to include uppercase letters
     * @param includeDigits whether to include digits
     * @param includeSpecial whether to include special characters
     * @return the generated password
     */
    public static String generatePassword(int length, boolean includeUppercase, boolean includeDigits, boolean includeSpecial) {
        if (length < 1) {
            throw new IllegalArgumentException("Password length must be at least 1");
        }

        String characterPool = LOWERCASE;
        if (includeUppercase) {
            characterPool += UPPERCASE;
        }
        if (includeDigits) {
            characterPool += DIGITS;
        }
        if (includeSpecial) {
            characterPool += SPECIAL_CHARACTERS;
        }

        if (characterPool.isEmpty()) {
            throw new IllegalArgumentException("Character pool cannot be empty");
        }

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characterPool.length());
            password.append(characterPool.charAt(index));
        }
        return password.toString();
    }
}