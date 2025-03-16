package Interfaces;

/**
 * Interface representing a password.
 */

public interface Passwords {

    /**
     * Return the password.
     * @return the password
     */
    String getPassword();

    /**
     * Sets the password to be stored.
     * @param password the password to be stored
     */
    void setPassword(String password);

    /**
     * Return the type of the password.
     * @return the type of the password
     */
    PasswordType getType();

    /**
     * Shows the password.
     */
    void show();
}